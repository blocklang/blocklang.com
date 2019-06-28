package com.blocklang.marketplace.task;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.jgit.lib.Ref;

import com.blocklang.core.git.GitUtils;
import com.blocklang.marketplace.constant.MarketplaceConstant;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.data.ComponentJson;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.skuzzle.semantic.Version;

/**
 * 解析组件库的 component.json 的文件
 * 
 * <p>
 * 这个一个分组任务，其中包含多项子任务
 * </p>
 * 
 * @author Zhengwei Jin
 *
 */
public class ComponentJsonParseGroupTask extends AbstractRepoPublishTask {

	private ComponentRepoDao componentRepoDao;
	
	public ComponentJsonParseGroupTask(MarketplacePublishContext context, 
			ComponentRepoDao componentRepoDao) {
		super(context);

		this.componentRepoDao = componentRepoDao;
	}

	/**
	 * 注意，解析出的数据已存到 MarketplacePublishContext 中，所以只需要返回解析是否成功。
	 */
	@Override
	public Optional<Boolean> run() {
		boolean success = true;
		// 从源代码托管网站下载组件的源代码
		logger.info("开始下载组件仓库的源码");
		GitSyncComponentRepoTask componentRepoTask = new GitSyncComponentRepoTask(context);
		Optional<Boolean> gitSyncOption = componentRepoTask.run();
		success = gitSyncOption.isPresent();
		if(success) {
			logger.info("完成");
		} else {
			logger.error("失败");
		}
		
		// 获取组件库的最新的 tag（即最新的发行版）
		String componentRepoLatestRefName = null;
		String componentRepoLatestVersion = null;
		if(success) {
			logger.info("开始获取最新的 Git Tag");
			ComponentRepoLatestTagFetchTask gitTagFetchTask = new ComponentRepoLatestTagFetchTask(context);
			Optional<Ref> gitTagFetchTaskOption = gitTagFetchTask.run();
			success = gitTagFetchTaskOption.isPresent();
			if(success) {
				componentRepoLatestRefName = gitTagFetchTaskOption.get().getName();
				Optional<String> versionOption = GitUtils.getVersionFromRefName(componentRepoLatestRefName);
				if(versionOption.isPresent()) {
					String version = versionOption.get();
					if(!Version.isValidVersion(version)) {
						logger.error("{0} 不是有效的语义化版本号，请调整后重试", version);
						success = false;
					} else {
						componentRepoLatestVersion = version;
						context.setComponentRepoLatestVersion(version);
						logger.info("完成，组件仓库的最新版本为 {0}", version);
					}
				} else {
					success = false;
				}
			} else {
				logger.error("组件仓库中没有找到发布的版本，请为仓库标注 tag 后再重试");
			}
		}

		// 从最新的 git tag 中查找 component.json 文件
		// 并获取 component.json 文件中的内容
		String componentJsonContent = null;
		if(success) {
			logger.info("在发行版 {0} 的根目录下查找 {1} 文件", componentRepoLatestVersion, MarketplaceConstant.FILE_NAME_COMPONENT);
			ComponentJsonFetchTask task = new ComponentJsonFetchTask(context, componentRepoLatestRefName);
			Optional<String> contentOption = task.run();
			success = contentOption.isPresent();
			if(success) {
				componentJsonContent = contentOption.get();
				logger.info("存在 {0} 文件", MarketplaceConstant.FILE_NAME_COMPONENT);
			} else {
				logger.error("没有找到 {0} 文件", MarketplaceConstant.FILE_NAME_COMPONENT);
			}
		}
		
		// 将 json 字符串转换为 java 对象
		ComponentJson componentJson = null;
		if(success) {
			logger.info("将{0} 中的文本转换为 java 对象", MarketplaceConstant.FILE_NAME_COMPONENT);
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				componentJson = objectMapper.readValue(componentJsonContent, ComponentJson.class);
				// 在这里往 context 中保存，这样在后续的校验子任务中，可以从 context 中获取 componentJson
				context.setComponentJson(componentJson);
				logger.info("转换完成");
			} catch (IOException e) {
				logger.error("转换失败");
				logger.error(e);
				success = false;
			}
		}

		// 校验 component.json 的 schema 和值的有效性
		if(success) {
			logger.info("校验 {0} 文件的 schema 和值", MarketplaceConstant.FILE_NAME_COMPONENT);
			
			ComponentJsonValidateTask componentJsonValidateTask = new ComponentJsonValidateTask(context, componentRepoDao);
			success = componentJsonValidateTask.run().isPresent();
			if(success) {
				logger.error("校验通过");
			}else {
				logger.error("校验未通过");
			}
		}
		
		if(success) {
			return Optional.of(true);
		}
		return Optional.empty();
	}

}
