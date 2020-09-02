package com.blocklang.release.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.service.PropertyService;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.service.ProjectDependencyService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.BuildResult;
import com.blocklang.release.constant.ReleaseMethod;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.constant.TargetOs;
import com.blocklang.release.dao.AppDao;
import com.blocklang.release.dao.AppReleaseDao;
import com.blocklang.release.dao.AppReleaseFileDao;
import com.blocklang.release.dao.AppReleaseRelationDao;
import com.blocklang.release.dao.ProjectBuildDao;
import com.blocklang.release.dao.ProjectReleaseTaskDao;
import com.blocklang.release.dao.RepositoryTagDao;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.model.AppReleaseRelation;
import com.blocklang.release.model.ProjectBuild;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.model.RepositoryTag;
import com.blocklang.release.service.BuildService;
import com.blocklang.release.task.AppBuildContext;
import com.blocklang.release.task.ClientDistCopyTask;
import com.blocklang.release.task.DojoBuildTask;
import com.blocklang.release.task.DojoCodemodsTask;
import com.blocklang.release.task.GitSyncProjectTemplateTask;
import com.blocklang.release.task.GitTagTask;
import com.blocklang.release.task.MavenInstallTask;
import com.blocklang.release.task.MavenPomConfigTask;
import com.blocklang.release.task.ProjectModelWriteTask;
import com.blocklang.release.task.ProjectTemplateCopyTask;
import com.blocklang.release.task.YarnTask;

@Service
public class BuildServiceImpl implements BuildService {

	@Autowired
	private PropertyService propertyService;
	@Autowired
	private RepositoryTagDao repositoryTagDao;
	@Autowired
	private ProjectBuildDao projectBuildDao;
	@Autowired
	private ProjectReleaseTaskDao projectReleaseTaskDao;
	@Autowired
	private AppDao appDao;
	@Autowired
	private AppReleaseDao appReleaseDao;
	@Autowired
	private AppReleaseRelationDao appReleaseRelationDao;
	@Autowired
	private AppReleaseFileDao appReleaseFileDao;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private ProjectDependencyService projectDependenceService;
	@Autowired
	private RepositoryResourceService projectResourceService;
	
	@Override
	public void build(Repository repository, ProjectReleaseTask releaseTask) {
		StopWatch stopWatch = StopWatch.createStarted();

		// 默认从 11.0.2 开始"
		String jdkVersion = appReleaseDao.findById(releaseTask.getJdkReleaseId()).map(AppRelease::getVersion).orElse("11.0.2");;
		
		String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).get();
		String mavenRootPath = propertyService.findStringValue(CmPropKey.MAVEN_ROOT_PATH).get();
		// FIXME: 本应校验是不是有效的 git 仓库，但是因为后续会重新设计这一块，因此暂不校验。
		String templateProjectGitUrl = propertyService.findStringValue(CmPropKey.TEMPLATE_PROJECT_GIT_URL).get();
		
		AppBuildContext context = new AppBuildContext(
				dataRootPath, 
				mavenRootPath,
				templateProjectGitUrl,
				repository.getCreateUserName(),
				repository.getName(),
				releaseTask.getVersion(),
				repository.getDescription(),
				jdkVersion);
		context.setProjectId(repository.getId());
		
		// 需要存储日志文件名，当读取历史日志时，就可以根据此字段定位到日志文件。
		releaseTask.setLogFileName(context.getLogFileName());
		projectReleaseTaskDao.save(releaseTask);
		
		// 以下三行是设置 websocket 消息的参数
		context.setSendMessage(true);
		context.setMessagingTemplate(messagingTemplate);
		context.setTaskId(releaseTask.getId());

		context.info(StringUtils.repeat("=", 60));
		context.info("开始发布 @{0}/{1} 项目", repository.getCreateUserName(), repository.getName());
		
		boolean success = true;
		
		context.info(StringUtils.repeat("-", 45));
		context.info("一、开始复制项目模板");
		// 从 master 分支下载或更新项目模板
		GitSyncProjectTemplateTask gitSyncProjectTemplateTask = new GitSyncProjectTemplateTask(context);
		Optional<Boolean> gitSyncOption = gitSyncProjectTemplateTask.run();
		success = gitSyncOption.isPresent();
		if(success) {
			context.info("完成");
		} else {
			context.error("失败");
		}
		
		if(success) {
			context.info("开始将模板代码复制到项目文件夹中");
			ProjectTemplateCopyTask copyTask = new ProjectTemplateCopyTask(context);
			Optional<Boolean> copyTaskOption = copyTask.run();
			
			success = copyTaskOption.isPresent();
		}
		if(success) {
			context.info("完成");
		} else {
			context.error("失败");
		}
		
		// 判断 git tag 是否已存在
		// 如果已存在，则不添加标签，而是直接打印信息，并进行下一个环节
		Integer projectTagId = null; // 在后续流程中使用。
		if(success) {
			// 在 git 仓库上添加标签
			context.info(StringUtils.repeat("-", 45));
			context.info("二、为 git 仓库添加附注标签");
			
			// 先判断是否存在 git 仓库，如果不存在则给出提示
			if(!GitUtils.isGitRepo(context.getGitRepositoryDirectory())) {
				success = false;
				context.error("{0} 不是有效的 git 仓库", context.getGitRepositoryDirectory().toString());
			}
			
			if(success) {
				GitTagTask gitTagTask = new GitTagTask(context);
				if(gitTagTask.exists()) {
					context.info("git 仓库上已存在 {0} 标签", context.getTagName());
				}else {
					Optional<String> tagIdOption = gitTagTask.run();
					success = tagIdOption.isPresent();
					if (tagIdOption.isPresent()) {
						context.info("完成");
						context.info(StringUtils.repeat("-", 45));
						
						success = true;
						String tagId = tagIdOption.get();
						
						// 在数据库中存储 git 标签信息
						context.info("在数据库中存储 git 标签信息");
						// 判断数据库中是否已存在标签信息
						projectTagId = repositoryTagDao.findByRepositoryIdAndVersion(repository.getId(), releaseTask.getVersion()).map(projectTag -> {
							context.info("在数据库表中已存在 {0} 标签信息，开始更新标签信息", context.getTagName());
							
							projectTag.setGitTagId(tagId);
							projectTag.setLastUpdateUserId(releaseTask.getCreateUserId());
							projectTag.setLastUpdateTime(LocalDateTime.now());
							Integer savedProjectTagId = repositoryTagDao.save(projectTag).getId();
							
							context.info("更新完成");
							return savedProjectTagId;
						}).orElseGet(() -> {
							context.info("往数据库表中新增 {0} 标签信息", context.getTagName());
							
							RepositoryTag projectTag = new RepositoryTag();
							projectTag.setRepositoryId(repository.getId());
							projectTag.setVersion(releaseTask.getVersion());
							projectTag.setGitTagId(tagId);
							projectTag.setCreateUserId(releaseTask.getCreateUserId());
							projectTag.setCreateTime(LocalDateTime.now());
							Integer savedProjectTagId = repositoryTagDao.save(projectTag).getId();
							
							context.info("新增完成");
							return savedProjectTagId;
						});
					}
				}
			}
		}
		
		if(success) {
			context.info(StringUtils.repeat("-", 45));
			context.info("三、开始准备项目模型数据");
			// 生成模型信息
			ProjectModelWriteTask projectModelWriteTask = new ProjectModelWriteTask(context, projectDependenceService, projectResourceService);
			success = projectModelWriteTask.run().isPresent();
			
			if(success) {
				context.info("完成");
			}else {
				context.error("失败");
			}
		}
		
		// 开始对项目进行个性化配置，并生成代码
		if(success) {
			context.info(StringUtils.repeat("-", 45));
			context.info("四、开始配置项目");
			// TODO: 完善 success 校验
			
			// 注意，这里不仅仅支持 dojo app，还要能支持 react，vue，angular 等 app
			// TODO: 此处需加一个判断，来确定是要生成 dojo app
			// 1. dojo 项目
			context.info("1. 开始配置 dojo 项目，并生成 Dojo APP 源代码");
			DojoCodemodsTask dojoCodemodsTask = new DojoCodemodsTask(context);
			success = dojoCodemodsTask.run().isPresent();
			if(success) {
				context.info("完成");
			}else {
				context.error("失败");
			}
			
			if(success) {
				// 2. spring boot 项目
				context.info("2. 开始配置 spring boot 项目");
				// 2.1. 配置 pom.xml 文件
				MavenPomConfigTask pomConfigTask = new MavenPomConfigTask(context);
				success = pomConfigTask.run().isPresent();
				
				if(success) {
					context.info("完成");
				}else {
					context.error("失败");
				}
			}
		}
		
		Integer projectBuildId = null;
		
		if(success) {
			context.info(StringUtils.repeat("-", 45));
			context.info("五、开始构建项目");
			
			context.info("往数据库中存储项目构建信息");
			// 注意，因为每次从新构建，都是全新的开始，所以如果已存在，则删除
			projectBuildId = saveProjectBuild(releaseTask, projectTagId);
			context.info("完成");
		}

		// 开始构建 dojo 项目
		// 1. 安装依赖
		if(success) {
			// 因为使用 npm 或 cnpm 会出现 package 下载不全的问题，所以改为 yarn
			// 并且 yarn 会在本地缓存 package，避免重复下载
			context.info("开始执行 yarn 命令");
			YarnTask yarnTask = new YarnTask(context);
			success = yarnTask.run().isPresent();
			
			// 日志文件中增加一个换行符
			context.println();
			if(success) {
				context.info("完成");
			}else {
				context.error("失败");
			}
		}
		
		// 2. 构建 dojo 项目
		if(success) {
			context.info("开始构建 dojo 项目");
			context.info("开始执行 dojo build --mode dist 命令");
			
			DojoBuildTask dojoBuildTask = new DojoBuildTask(context);
			success = dojoBuildTask.run().isPresent();
			
			// 日志文件中增加一个换行符
			context.println();
			if(success) {
				context.info("完成");
			}else {
				context.error("失败");
			}
		}

		// 3. 将发布的 dojo 代码复制到 spring boot 的 static 和 templates 文件夹中
		if(success) {
			context.info("开始将发布的客户端代码复制到 Spring Boot 的 static 和 templates 文件夹中");
			ClientDistCopyTask copyTask = new ClientDistCopyTask(context);
			success = copyTask.run().isPresent();
			if(success) {
				context.info("完成");
			}else {
				context.error("失败");
			}
		}

		// 开始构建 spring boot 项目
		if(success) {
			context.info("开始执行 mvnw clean install 命令");
			MavenInstallTask mavenInstallTask = new MavenInstallTask(context);
			success = mavenInstallTask.run().isPresent();
			if(success) {
				context.info("完成");
			}else {
				context.error("失败");
			}
		}

		// 修改构建状态
		if(success) {
			context.info("恭喜！构建成功");
		}
		
		// 必须要添加此是否为 null 判断，因为上述逻辑可能会出现 projectBuildId 为 null 的情况
		if(projectBuildId != null) {
			context.info("更新构建结果");
			BuildResult buildResult = success ? BuildResult.PASSED : BuildResult.FAILED;
			projectBuildDao.findById(projectBuildId).ifPresent(projectBuild -> {
				projectBuild.setEndTime(LocalDateTime.now());
				projectBuild.setBuildResult(buildResult);
				projectBuild.setLastUpdateTime(LocalDateTime.now());
				projectBuild.setLastUpdateUserId(releaseTask.getCreateUserId());
				projectBuildDao.save(projectBuild);
			});
			context.info("更新完成");
		}

		// 构建成功后，存储 APP 发布信息
		if(success) {
			saveAppReleaseInfo(repository, releaseTask, context);
		}
		
		// 更新发布任务的状态
		ReleaseResult releaseResult = success ? ReleaseResult.PASSED : ReleaseResult.FAILED;
		
		releaseTask.setEndTime(LocalDateTime.now());
		releaseTask.setReleaseResult(releaseResult);
		releaseTask.setLastUpdateTime(LocalDateTime.now());
		releaseTask.setLastUpdateUserId(releaseTask.getCreateUserId());
		projectReleaseTaskDao.save(releaseTask);
		
		if(success) {
			context.info("发布完成");
		} else {
			context.error("发布失败");
		}
		
		stopWatch.stop();
		long seconds = stopWatch.getTime(TimeUnit.SECONDS);
		context.info("共耗时：{0} 秒", seconds);
		context.info(StringUtils.repeat("=", 60));
		
		context.finished(releaseResult);
	}

	// FIXME: 注意，在此处增加 @Transactional 不会生效，提取到另一个 service 中
	@Transactional
	private void saveAppReleaseInfo(Repository project, ProjectReleaseTask releaseTask, AppBuildContext context) {
		// 1. 获取 APP 信息
		appDao.findByProjectId(project.getId()).map(app -> {
			// 2. 存储 APP_RELEASE
			AppRelease appRelease = new AppRelease();
			appRelease.setAppId(app.getId());
			appRelease.setVersion(releaseTask.getVersion());
			appRelease.setTitle(releaseTask.getTitle());
			appRelease.setDescription(releaseTask.getDescription());
			appRelease.setReleaseMethod(ReleaseMethod.AUTO);
			appRelease.setReleaseTime(LocalDateTime.now());
			appRelease.setCreateTime(LocalDateTime.now());
			appRelease.setCreateUserId(releaseTask.getCreateUserId());
			
			return appReleaseDao.save(appRelease);
		}).map(appRelease -> {
			// 3. 存储 APP_RELEASE_RELATION
			AppReleaseRelation relation = new AppReleaseRelation();
			relation.setAppReleaseId(appRelease.getId());
			relation.setDependAppReleaseId(releaseTask.getJdkReleaseId());
			appReleaseRelationDao.save(relation);
			
			// 4. 存储 APP_RELEASE_FILE
			AppReleaseFile file = new AppReleaseFile();
			file.setAppReleaseId(appRelease.getId());
			file.setTargetOs(TargetOs.ANY);
			file.setArch(Arch.ANY);
			file.setFileName(context.getMavenInstallJarFileName());
			file.setFilePath(context.getMavenInstallJarRelativePath().toString());
			file.setCreateTime(LocalDateTime.now());
			file.setCreateUserId(releaseTask.getCreateUserId());
			return appReleaseFileDao.save(file);
		});
	}

	// FIXME: 注解不会生效
	@Transactional
	private Integer saveProjectBuild(ProjectReleaseTask releaseTask, Integer projectTagId) {
		projectBuildDao.findByProjectTagId(projectTagId).ifPresent(projectBuild -> {
			projectBuildDao.delete(projectBuild);
		});
		
		ProjectBuild projectBuild = new ProjectBuild();
		projectBuild.setCreateTime(LocalDateTime.now());
		projectBuild.setCreateUserId(releaseTask.getCreateUserId());
		projectBuild.setProjectTagId(projectTagId);
		projectBuild.setStartTime(LocalDateTime.now());
		projectBuild.setBuildResult(BuildResult.STARTED);
		return projectBuildDao.save(projectBuild).getId();
	}

	@Async
	@Override
	public void asyncBuild(Repository project, ProjectReleaseTask releaseTask) {
		this.build(project, releaseTask);
	}
	
}
