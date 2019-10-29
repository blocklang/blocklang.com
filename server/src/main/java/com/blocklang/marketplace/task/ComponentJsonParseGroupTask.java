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
		// 校验远程仓库是否存在
		String gitUrl = context.getPublishTask().getGitUrl();
		logger.info("开始校验 {0} 仓库是否存在", gitUrl);
		success = GitUtils.isValidRemoteRepository(gitUrl);
		if(success) {
			logger.info("存在");
		}else {
			logger.error("不存在");
		}
		
		// 从源代码托管网站下载组件的源代码
		if(success) {
			logger.info("开始下载组件仓库的源码");
			GitSyncComponentRepoTask componentRepoTask = new GitSyncComponentRepoTask(context);
			Optional<Boolean> gitSyncOption = componentRepoTask.run();
			success = gitSyncOption.isPresent();
			if(success) {
				logger.info("完成");
			} else {
				logger.error("失败");
			}
		}
		
		// 获取组件库的最新的 tag（即最新的发行版）
		String componentRepoLatestRefName = null;
		String componentRepoLatestVersion = null;
		if(success) {
			logger.info("开始获取最新的 Git Tag");
			ComponentRepoLatestTagFindTask gitTagFindTask = new ComponentRepoLatestTagFindTask(context);
			Optional<Ref> gitTagFindTaskOption = gitTagFindTask.run();
			success = gitTagFindTaskOption.isPresent();
			if(success) {
				componentRepoLatestRefName = gitTagFindTaskOption.get().getName();
				Optional<String> versionOption = GitUtils.getVersionFromRefName(componentRepoLatestRefName);
				if(versionOption.isPresent()) {
					String version = versionOption.get();
					if(!Version.isValidVersion(version)) {
						logger.error("{0} 不是有效的语义化版本号，请调整后重试", version);
						success = false;
					} else {
						componentRepoLatestVersion = version;
						context.setComponentRepoLatestVersion(version);
						
						// 再保存一份 tag
						context.setComponentRepoLatestTagName(GitUtils.getTagName(componentRepoLatestRefName).orElse(null));
						
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
				logger.info("存在");
			} else {
				logger.error("没有找到");
			}
		}
		
		// 将 json 字符串转换为 java 对象
		ComponentJson componentJson = null;
		if(success) {
			logger.info("将 {0} 中的文本转换为 java 对象", MarketplaceConstant.FILE_NAME_COMPONENT);
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
				logger.info("校验通过");
			}else {
				logger.error("校验未通过");
			}
		}
		
		// 准备好 componentJson 后，再先构建源代码，如果构建失败，则结束注册流程
		// 如果是 ide 版、非标准库的组件库，则要构建项目
		// 该操作只有在完成上述校验之后才有意义
		// 要先切换到对应的 tag，构建完后，再切换回 master 分支
		// 构建完后，要移动到指定的文件夹下，然后删除构建生成的所有文件
		if(success) {
			if(!componentJson.isStd() && componentJson.isDev()) {
				// TODO: 当加入 java 等组件库后，需要再加一层判断，确保是 dojo app
				logger.info("该组件库属于 ide 版本，并且不属于标准库，需要构建为 dojo library");
				
				DojoBuildAppGroupTask dojoBuildAppGroupTask = new DojoBuildAppGroupTask(context);
				success = dojoBuildAppGroupTask.run().isPresent();
				if(success) {
					logger.info("构建完成");
				}else {
					logger.error("构建失败");
				}
			}
		}
		
		if(success) {
			return Optional.of(true);
		}
		return Optional.empty();
	}

}
