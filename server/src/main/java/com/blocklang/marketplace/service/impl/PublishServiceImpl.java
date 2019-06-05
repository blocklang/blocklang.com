package com.blocklang.marketplace.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.service.PropertyService;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ComponentRepoPublishTaskDao;
import com.blocklang.marketplace.dao.ComponentRepoRegistryDao;
import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.model.ComponentRepoRegistry;
import com.blocklang.marketplace.service.PublishService;
import com.blocklang.marketplace.task.BlockLangJsonFetchTask;
import com.blocklang.marketplace.task.GitSyncComponentRepoTask;
import com.blocklang.marketplace.task.LatestGitTagFetchTask;
import com.blocklang.marketplace.task.MarketplacePublishContext;
import com.blocklang.marketplace.task.TaskLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.skuzzle.semantic.Version;

@Service
public class PublishServiceImpl implements PublishService {

	@Autowired
	private PropertyService propertyService;
	@Autowired
	private ComponentRepoPublishTaskDao componentRepoPublishTaskDao;
	@Autowired
	private ComponentRepoRegistryDao componentRepoRegistryDao;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	
	@Async
	@Override
	public void asyncPublish(ComponentRepoPublishTask publishTask) {
		this.publish(publishTask);
	}

	@Override
	public void publish(ComponentRepoPublishTask publishTask) {
		StopWatch stopWatch = StopWatch.createStarted();

		String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).get();
		MarketplacePublishContext context = new MarketplacePublishContext(dataRootPath, publishTask.getGitUrl());
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
		logger.info("开始发布 @{0}/{1} 组件库", context.getOwner(), context.getRepoName());
		
		boolean success = true;

		// 从源代码托管网站下载组件的源代码
		logger.info(StringUtils.repeat("-", 45));
		logger.info("一、开始获取组件库源码");
		GitSyncComponentRepoTask componentRepoTask = new GitSyncComponentRepoTask(context);
		Optional<Boolean> gitSyncOption = componentRepoTask.run();
		success = gitSyncOption.isPresent();
		if(success) {
			logger.info("完成");
		} else {
			logger.error("失败");
		}
		
		String refName = null;
		String tagName = null;
		if(success) {
			logger.info(StringUtils.repeat("-", 45));
			logger.info("二、开始获取最新的 Git Tag");
			LatestGitTagFetchTask gitTagFetchTask = new LatestGitTagFetchTask(context);
			Optional<Ref> gitTagFetchTaskOption = gitTagFetchTask.run();
			success = gitTagFetchTaskOption.isPresent();
			
			if(success) {
				refName = gitTagFetchTaskOption.get().getName();
				tagName = refName.substring(Constants.R_TAGS.length());
				logger.info("完成，最新的 git tag 版本为 {0}", tagName);
			} else {
				logger.error("在该仓库中没有找到 git tag，请为仓库标注 tag 后再重试");
			}
		}
		
		
		// 从最新的 git tag 中查找 blocklang.json 文件
		String blocklangJsonContent = null;
		if(success) {
			logger.info(StringUtils.repeat("-", 45));
			logger.info("三、校验仓库根目录下的 blocklang.json 文件");
			
			logger.info("在 Git Tag {0} 的根目录下查找 blocklang.json 文件", tagName);
			
			BlockLangJsonFetchTask task = new BlockLangJsonFetchTask(context, refName);
			Optional<String> contentOption = task.run();
			success = contentOption.isPresent();
			
			if(success) {
				blocklangJsonContent = contentOption.get();
				logger.info("存在 blocklang.json 文件");
			} else {
				logger.error("没有找到 blocklang.json 文件");
			}
		}
		
		// 将 json 字符串转换为 java 对象
		ComponentRepoInfo repoInfo = null;
		if(success) {
			logger.info("将 blocklang.json 内容转换为 java 对象");
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				repoInfo = objectMapper.readValue(blocklangJsonContent, ComponentRepoInfo.class);
				logger.error("转换完成");
				success = true;
			} catch (IOException e) {
				logger.error("转换失败");
				logger.error(e);
				success = false;
			}
		}
		
		// 校验 blocklang.json 中的字段
		// name
		// 1. 不能为空
		// 2. 长度不能超过64个字节
		// 3. 只能包含字母、数字、中划线和下划线
		// 4. 同一个发布者没有发不过此名称的组件库
		// version
		// 1. 不能为空
		// 2. 必须是有效的语义化版本
		// 3. 必须大于最新的版本号
		// category
		// 1. 不能为空
		// 2. 只能是 “Widget”（不区分大小写）
		// language
		// 1. 不能为空
		// 2. 只能是“Typescript”、“Java”（不区分大小写）
		// description
		// 1. 长度不能超过512个字节
		// icon
		// 1. 长度不能超过64个字节
		if(success) {
			// name
			boolean nameHasError = false;
			String name = repoInfo.getName();
			if(StringUtils.isBlank(name)) {
				logger.error("name - 值不能为空");
				nameHasError = true;
			}
			String trimedName = name.trim();
			if(!nameHasError) {
				// 之所以取 60 而并不是 64，因为 60 好记
				if(com.blocklang.core.util.StringUtils.byteLength(trimedName) > 60) {
					logger.error("name - 值的长度不能超过60个字节(一个汉字占两个字节)");
					nameHasError = true;
				}
			}
			if(!nameHasError) {
				//校验：只支持英文字母、数字、中划线(-)、下划线(_)、点(.)
				String regEx = "^[a-zA-Z0-9\\-\\w]+$";
				Pattern pattern = Pattern.compile(regEx);
				Matcher matcher = pattern.matcher(trimedName);
				if(!matcher.matches()) {
					logger.error("name - 值只支持英文字母、数字、中划线(-)、下划线(_)、点(.)，‘{0}’中包含非法字符", trimedName);
					nameHasError = true;
				}
			}
			if(!nameHasError) {
				if(componentRepoRegistryDao.findByNameAndCreateUserId(trimedName, publishTask.getCreateUserId()).isPresent()) {
					logger.error("name - {0} 下已注册名为 {1} 的组件库，请换一个名字", publishTask.getCreateUserName(), trimedName);
					nameHasError = true;
				}
			}
			
			// version
			boolean versionHasError = false;
			String version = repoInfo.getVersion();
			if(StringUtils.isBlank(version)) {
				logger.error("version - 值不能为空");
				versionHasError = true;
			}
			String trimedVersion = version.trim();
			if(!versionHasError) {
				if(!Version.isValidVersion(trimedVersion)) {
					logger.error("version - 值 {0} 不是有效的语义化版本");
					versionHasError = true;
				}
			}
			if(!versionHasError) {
				// 获取最新发布的版本号
				Optional<ComponentRepoRegistry> registryOption = componentRepoRegistryDao.findByNameAndCreateUserId(trimedName, publishTask.getCreateUserId());
				if(registryOption.isPresent()) {
					Version previousVersion = Version.parseVersion(registryOption.get().getVersion(), true);
					Version currentVersion = Version.parseVersion(trimedVersion, true);
					if(!currentVersion.isGreaterThan(previousVersion)) {
						logger.error("version - 版本号应大于项目最新的版本号，但 {} 没有大于上一个版本号 {}", trimedVersion, registryOption.get().getVersion());
						versionHasError = true;
					}
				}
			}
			
			// category
			boolean categoryHasError = false;
			String category = repoInfo.getCategory();
			if(StringUtils.isBlank(category)) {
				logger.error("category - 值不能为空");
				categoryHasError = true;
			}
			if(!categoryHasError) {
				if(!category.trim().equalsIgnoreCase("Widget")) {
					logger.error("category - 值只能是 Widget");
					categoryHasError = true;
				}
			}
			
			// language
			boolean languageHasError = false;
			String language = repoInfo.getLanguage();
			if(StringUtils.isBlank(language)) {
				logger.error("language - 值不能为空");
				languageHasError = true;
			}
			String trimedLanguage = language.trim();
			if(!languageHasError) {
				if(!trimedLanguage.equalsIgnoreCase("Java") && !trimedLanguage.equalsIgnoreCase("TypeScript")) {
					logger.error("language - 值只能是 Java 或 TypeScript");
					languageHasError = true;
				}
			}
			
			// description
			boolean descriptionHasError = false;
			String description = repoInfo.getDescription();
			if(com.blocklang.core.util.StringUtils.byteLength(description) > 500) {
				logger.error("description - 值的长度不能超过500个字节(一个汉字占两个字节)");
				descriptionHasError = true;
			}
			
			// icon
			boolean iconHasError = false;
			String icon = repoInfo.getIcon();
			if(com.blocklang.core.util.StringUtils.byteLength(icon) > 60) {
				logger.error("icon - 值的长度不能超过60个字节(一个汉字占两个字节)");
				iconHasError = true;
			}
			
			success = !(nameHasError || versionHasError || categoryHasError || languageHasError || descriptionHasError || iconHasError);
		}
		
		ComponentRepoRegistry savedRepoRegistry = null;
		if(success) {
			// 注册组件库
			logger.info(StringUtils.repeat("-", 45));
			logger.info("四、开始保存组件库基本信息");
			
			ComponentRepoRegistry repoRegistry = new ComponentRepoRegistry();
			repoRegistry.setGitRepoUrl(publishTask.getGitUrl());
			repoRegistry.setGitRepoWebsite(context.getWebsite());
			repoRegistry.setGitRepoOwner(context.getOwner());
			repoRegistry.setGitRepoName(context.getRepoName());
			repoRegistry.setName(repoInfo.getName().trim()); // name 必填
			repoRegistry.setVersion(repoInfo.getVersion().trim()); // version 必填
			repoRegistry.setLabel(repoInfo.getDisplayName());
			repoRegistry.setDescription(repoInfo.getDescription());
			repoRegistry.setCategory(RepoCategory.fromValue(repoInfo.getCategory().trim()));
			repoRegistry.setLanguage(Language.fromValue(repoInfo.getLanguage().trim()));
			if(StringUtils.isNotBlank(repoInfo.getIcon())) {
				repoRegistry.setLogoPath(repoInfo.getIcon());
			}
			repoRegistry.setCreateUserId(publishTask.getCreateUserId());
			repoRegistry.setCreateTime(LocalDateTime.now());
			
			savedRepoRegistry = componentRepoRegistryDao.save(repoRegistry);
			logger.info("保存成功");
		}
		
		// 以上是仓库一级的校验，校验通过后，登记组件库信息。
		// 接下来逐个做组件一级的校验
		// 先检查总共有多少个组件，然后逐个解析组件的基本信息，属性和事件等
		
		// TODO: 先看是否能编译成功？
		
		String[] widgets = null;
		if(success) {
			// 当是 UI 部件时
			if("Widget".equalsIgnoreCase(repoInfo.getCategory().trim()) && 
					"TypeScript".equalsIgnoreCase(repoInfo.getLanguage().trim())) {
				widgets = repoInfo.getWidgets();
				if(widgets.length == 0) {
					logger.info("blocklang.json 文件的 widgets 中没有配置 UI 部件");
					success = false;
				}
			}
		}
		
		// 遍历目录，按照默认的规则查找部件
		// 查询路径是 'src/widgets/{widgetName}/index.ts'
		// 校验规则需逐步完善和精准
		// 校验规则：
		// 1. 一个 UI 部件的目录下，应该包含以下文件：index.ts、designer.ts、changelog 文件夹中应该至少有一个 json 文件
		// 2. 解析 changelog 文件夹
		if(success) {
			logger.info(StringUtils.repeat("-", 45));
			logger.info("五、开始逐个校验 Widget");
			
			logger.info("在 blocklang.json 文件中共发现 {0} 个部件", widgets.length);
			for(int i = 0; i < widgets.length; i++) {
				String widgetPath = widgets[i];
				logger.info("{0} {1} 部件", i+1, widgetPath);
				
				Path widgetRootPath = context.getRepoSourceDirectory().resolve(widgetPath);
				// 判断是否存在 index.ts 文件
				if(Files.notExists(widgetRootPath.resolve("index.ts"))) {
					logger.error("    在 {0} 文件夹下缺失 index.ts 文件", widgetPath);
				}
				// 判断是否存在 designer.ts 文件
				if(Files.notExists(widgetRootPath.resolve("designer.ts"))) {
					logger.error("    在 {0} 文件夹下缺失 designer.ts 文件", widgetPath);
				}
				// 判断是否存在 changelog 文件夹
				if(Files.notExists(widgetRootPath.resolve("changelog"))) {
					logger.error("    在 {0} 文件夹下缺失 changelog 文件夹", widgetPath);
				}
				// changelog 文件夹不能为空
				if(widgetRootPath.resolve("changelog").toFile().list().length == 0) {
					logger.error("    在 {0}/changelog 文件夹下缺失 Widget 定义文件，如 0_1_0.json", widgetPath);
				}
				
				// 开始校验 changelog 文件
				
			}
			
			
			
		}
		
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
		
		// TODO:当组件库中的组件解析完成后，再填写发布时间
		
		stopWatch.stop();
		long seconds = stopWatch.getTime(TimeUnit.SECONDS);
		logger.info("共耗时：{0} 秒", seconds);
		logger.info(StringUtils.repeat("=", 60));
		
		// context.finished(releaseResult);
	}

}
