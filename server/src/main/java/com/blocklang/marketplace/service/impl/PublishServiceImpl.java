package com.blocklang.marketplace.service.impl;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.service.PropertyService;
import com.blocklang.marketplace.constant.MarketplaceConstant;
import com.blocklang.marketplace.dao.ApiChangeLogDao;
import com.blocklang.marketplace.dao.ApiComponentAttrDao;
import com.blocklang.marketplace.dao.ApiComponentAttrFunArgDao;
import com.blocklang.marketplace.dao.ApiComponentAttrValOptDao;
import com.blocklang.marketplace.dao.ApiComponentDao;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoPublishTaskDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.PublishService;
import com.blocklang.marketplace.task.ApiChangeLogParseGroupTask;
import com.blocklang.marketplace.task.ApiChangeLogsSetupGroupTask;
import com.blocklang.marketplace.task.ApiJsonParseGroupTask;
import com.blocklang.marketplace.task.ComponentJsonParseGroupTask;
import com.blocklang.marketplace.task.MarketplacePublishContext;
import com.blocklang.marketplace.task.TaskLogger;
import com.blocklang.release.constant.ReleaseResult;

@Service
public class PublishServiceImpl implements PublishService {

	@Autowired
	private PropertyService propertyService;
	@Autowired
	private ComponentRepoPublishTaskDao componentRepoPublishTaskDao;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiComponentDao apiComponentDao;
	@Autowired
	private ApiComponentAttrDao apiComponentAttrDao;
	@Autowired
	private ApiComponentAttrValOptDao apiComponentAttrValOptDao;
	@Autowired
	private ApiComponentAttrFunArgDao apiComponentAttrFunArgDao;
	@Autowired
	private ApiChangeLogDao apiChangeLogDao;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	
	@Async
	@Override
	public void asyncPublish(ComponentRepoPublishTask publishTask) {
		this.publish(publishTask);
	}

	// TODO: 先实现功能，再重构。
	@Override
	public void publish(ComponentRepoPublishTask publishTask) {
		StopWatch stopWatch = StopWatch.createStarted();

		String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).get();
		MarketplacePublishContext context = new MarketplacePublishContext(dataRootPath, publishTask);
		// 确保全程使用同一个 logger
		Path logFile = context.getRepoPublishLogFile();
		publishTask.setLogFileName(logFile.getFileName().toString());
		componentRepoPublishTaskDao.save(publishTask);
		
		TaskLogger logger = new TaskLogger(logFile);
		// 以下三行是设置 websocket 消息的参数
		// 设置日志组件，支持向远程发送消息
		// TODO: 以下接口是否可以简化？
		logger.setSendMessage(true);
		logger.setMessagingTemplate(messagingTemplate);
		logger.setTaskId(publishTask.getId());
		
		logger.info(StringUtils.repeat("=", 60));
		logger.info("开始发布 @{0}/{1} 组件库", 
				context.getLocalComponentRepoPath().getOwner(), 
				context.getLocalComponentRepoPath().getRepoName());
		
		boolean success = true;

		logger.info(StringUtils.repeat("-", 45));
		logger.info("一、开始解析组件仓库中的 {0}", MarketplaceConstant.FILE_NAME_COMPONENT);
		ComponentJsonParseGroupTask componentJsonParseTask = new ComponentJsonParseGroupTask(
				context,
				componentRepoDao);
		success = componentJsonParseTask.run().isPresent();
		if(success) {
			logger.info("解析完成");
		} else {
			logger.error("解析失败");
		}
		
		// 对 component.json 文件校验通过后
		// 开始下载 api 项目，并校验 api.json
		if(success) {
			logger.info(StringUtils.repeat("-", 45));
			logger.info("二、开始解析 API 库中的 {0}", MarketplaceConstant.FILE_NAME_API);
			ApiJsonParseGroupTask apiJsonParseGroupTask = new ApiJsonParseGroupTask(context);
			success = apiJsonParseGroupTask.run().isPresent();
		}
		if(success) {
			logger.info("解析完成");
		} else {
			logger.error("解析失败");
		}
		
		if(success) {
			logger.info(StringUtils.repeat("-", 45));
			logger.info("三、开始解析 API 库中的 change log 文件");
			ApiChangeLogParseGroupTask apiChangeLogParseGroupTask = new ApiChangeLogParseGroupTask(context, apiRepoDao, apiChangeLogDao);
			success = apiChangeLogParseGroupTask.run().isPresent();
		}
		if(success) {
			logger.info("解析完成");
		} else {
			logger.error("解析失败");
		}

		// 开始逐个版本的安装 API
		if(success) {
			logger.info(StringUtils.repeat("-", 45));
			logger.info("四、开始安装 API 变更文件");
			ApiChangeLogsSetupGroupTask task = new ApiChangeLogsSetupGroupTask(
					context, 
					componentRepoDao,
					componentRepoVersionDao,
					apiRepoDao,
					apiRepoVersionDao,
					apiComponentDao,
					apiComponentAttrDao,
					apiComponentAttrValOptDao,
					apiComponentAttrFunArgDao,
					apiChangeLogDao);
			success = task.run().isPresent();
			if(success) {
				logger.info("安装完成");
			} else {
				logger.error("安装失败");
			}
		}
		
		
		// TODO: 确保 component.json 中的 api.version 与通过 version 选中的 api.json 中的 version 值一致
		
		
		// 如果当前版本存在 changelog 文件，则先查找是否存在上一个版本
		// 如果存在上一个版本，则先复制上一个版本
		// 然后在上一个版本的基础上，应用本版本的变更
		
		// 注意：变更文件的名称，是与版本号保持一致的，会存在某个组件在某个版本中没有变更的情况
		// 所以要一直向上追溯，而不是只追溯到上一个版本
		
		if(success) {
			
			// 如果是第一个变动，则与上一个版本做比较
			// 如果是第二个及后续变动，则与上一个变动后的版本做比较
			// 如果是新增部件，则判断该部件名是否已被占用，如果已占用，则给出错误信息
			// 所以在校验时，要先应用所有变动，如果校验失败，则删除所有变动
			// 先找到上一个版本，然后复制一份上一版本的内容
			
			// 如果当前应用的不是最新版的 API，则已发布过，所以不需要再发布?
			// 如果第二版已发布过，但是这里又要发布第一版，如何确保 code 的唯一？
			// 所以，在第一次发布时，一定要第一版能够发布？
			// 或者在发布时，将前面的每一版都检查并发布一遍？
			
			// 只有执行成功的，才往 changelog 历史表中存
			// 出错了，要 rollback
		}
		
		// 基于上一个版本做校验
		// 为新组件生成 code
		// 保存组件库基本信息
		// 保存 API 库基本信息
		// 应用 API 变更
		// 保存 API 变更执行结果
		
		
		
		// 修改组件库和API 库的最近发布时间
		
		
		
		
		
		// API 项目，如果已保存，则不再保存。
		
		
		
		// 以上是仓库一级的校验，校验通过后，登记组件库信息。
		// 接下来逐个做组件一级的校验
		// 先检查总共有多少个组件，然后逐个解析组件的基本信息，属性和事件等
		
		// TODO: 先看是否能编译成功？
		// TODO: 以下逻辑要重写
//		String[] components = null;
//		if(success) {
//			// 当是 UI 部件时
//			if("Widget".equalsIgnoreCase(repoInfo.getCategory().trim()) && 
//					"TypeScript".equalsIgnoreCase(repoInfo.getLanguage().trim())) {
//				components = repoInfo.getComponents();
//				if(components.length == 0) {
//					logger.info("blocklang.json 文件的 widgets 中没有配置 UI 部件");
//					success = false;
//				}
//			}
//		}
		
		// 遍历目录，按照默认的规则查找部件
		// 查询路径是 'src/widgets/{widgetName}/index.ts'
		// 校验规则需逐步完善和精准
		// 校验规则：
		// 1. 一个 UI 部件的目录下，应该包含以下文件：index.ts、designer.ts、changelog 文件夹中应该至少有一个 json 文件
		// 2. 解析 changelog 文件夹
		
		
		// 如果解析出错了呢？？？？？？？？？？？？？？
		// 前面的操作撤销还是保留呢？
		// 通过写文档梳理。
		

		
		// TODO: 先读取 tag 列表，还是先检查 blocklang.json
		
		// 或者检测都通过之后，确定是一个有效的组件仓库之后，才开始存储数据
		// 所以，这个阶段，应该是做仓库一级的检测
		// 仓库一级的检测通过后，才允许注册组件仓库
		// 第二轮逐个做部件检测，检测通过的组件才能保存，检测没有通过的给出详细提示
		
		
		// 查找 git 仓库中的 tag
		// 找到最新的 tag
		// 从最新的 tag 中查找 blocklang.json 文件
		// 校验 blocklang.json 中的必填项等
		// 扫面项目中所有的 ui 部件，确认文件是否齐全
		// 扫描 changelog 文件夹，检测配置的是否准确
		// 当检测通过之后，才开始往数据库中存储 ui 部件的元数据
		// 编译 ts 文件？
		
		
		
		// 更新发布任务的状态
		ReleaseResult releaseResult = success ? ReleaseResult.PASSED : ReleaseResult.FAILED;
		
		publishTask.setEndTime(LocalDateTime.now());
		publishTask.setPublishResult(releaseResult);
		publishTask.setLastUpdateTime(LocalDateTime.now());
		publishTask.setLastUpdateUserId(publishTask.getCreateUserId());
		componentRepoPublishTaskDao.save(publishTask);
		
		if(success) {
			logger.info("发布完成");
		} else {
			logger.error("发布失败");
		}
		
		stopWatch.stop();
		long seconds = stopWatch.getTime(TimeUnit.SECONDS);
		logger.info("共耗时：{0} 秒", seconds);
		logger.info(StringUtils.repeat("=", 60));
		
		// context.finished(releaseResult);
	}

}
