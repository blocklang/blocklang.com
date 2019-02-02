package com.blocklang.release.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.blocklang.Config;
import com.blocklang.develop.model.Project;
import com.blocklang.git.GitUtils;
import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.BuildResult;
import com.blocklang.release.constant.ReleaseMethod;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.constant.TargetOs;
import com.blocklang.release.dao.AppDao;
import com.blocklang.release.dao.AppReleaseDao;
import com.blocklang.release.dao.AppReleaseFileDao;
import com.blocklang.release.dao.AppReleaseRelationDao;
import com.blocklang.release.dao.GitTagDao;
import com.blocklang.release.dao.ProjectBuildDao;
import com.blocklang.release.dao.ProjectReleaseTaskDao;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.model.AppReleaseRelation;
import com.blocklang.release.model.ProjectBuild;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.model.ProjectTag;
import com.blocklang.release.service.BuildService;
import com.blocklang.release.task.AppBuildContext;
import com.blocklang.release.task.ClientDistCopyTask;
import com.blocklang.release.task.DojoBuildTask;
import com.blocklang.release.task.GitSyncProjectTemplateTask;
import com.blocklang.release.task.GitTagTask;
import com.blocklang.release.task.MavenInstallTask;
import com.blocklang.release.task.NpmInstallTask;
import com.blocklang.release.task.ProjectTemplateCopyTask;

@Service
public class BuildServiceImpl implements BuildService {

	@Autowired
	private GitTagDao gitTagDao;
	
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
	
	@Override
	public void build(Project project, ProjectReleaseTask releaseTask) {
		StopWatch stopWatch = StopWatch.createStarted();

		String projectsRootPath = Config.BLOCK_LANG_ROOT_PATH;
		String mavenRootPath = Config.MAVEN_ROOT_PATH;
		String projectTemplateGitUrl = Config.PROJECT_TEMPLATE_GIT_URL;

		AppBuildContext context = new AppBuildContext(
				projectsRootPath, 
				mavenRootPath,
				projectTemplateGitUrl,
				project.getCreateUserName(),
				project.getProjectName(),
				releaseTask.getVersion());

		context.info(StringUtils.repeat("=", 60));
		context.info("开始发布 @{0}/{1} 项目", project.getCreateUserName(), project.getProjectName());
		
		boolean success = true;
		
		context.info(StringUtils.repeat("-", 45));
		context.info("一、开始复制项目模板");
		// 从 master 分支下载或更新项目模板
		context.info("从远程 git 服务器克隆或拉取最新的源码");
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
		
		// 在 git 仓库上添加标签
		context.info(StringUtils.repeat("-", 45));
		context.info("二、为 git 仓库添加附注标签");
		// 判断 git tag 是否已存在
		// 如果已存在，则不添加标签，而是直接打印信息，并进行下一个环节
		Integer projectTagId = null; // 在后续流程中使用。
		
		
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
					projectTagId = gitTagDao.findByProjectIdAndVersion(project.getId(), releaseTask.getVersion()).map(projectTag -> {
						context.info("在数据库表中已存在 {0} 标签信息，开始更新标签信息", context.getTagName());
						
						projectTag.setGitTagId(tagId);
						projectTag.setLastUpdateUserId(releaseTask.getCreateUserId());
						projectTag.setLastUpdateTime(LocalDateTime.now());
						Integer savedProjectTagId = gitTagDao.save(projectTag).getId();
						
						context.info("更新完成");
						return savedProjectTagId;
					}).orElseGet(() -> {
						context.info("往数据库表中新增 {0} 标签信息", context.getTagName());
						
						ProjectTag projectTag = new ProjectTag();
						projectTag.setProjectId(project.getId());
						projectTag.setVersion(releaseTask.getVersion());
						projectTag.setGitTagId(tagId);
						projectTag.setCreateUserId(releaseTask.getCreateUserId());
						projectTag.setCreateTime(LocalDateTime.now());
						Integer savedProjectTagId = gitTagDao.save(projectTag).getId();
						
						context.info("新增完成");
						return savedProjectTagId;
					});
				}
			}
		}
		
		Integer projectBuildId = null;
		
		if(success) {
			context.info(StringUtils.repeat("-", 45));
			context.info("三、开始构建项目");
			
			context.info("往数据库中存储项目构建信息");
			// 注意，因为每次从新构建，都是全新的开始，所以如果已存在，则删除
			projectBuildId = saveProjectBuild(releaseTask, projectTagId);
			context.info("完成");
		}
		
		// 开始对项目进行个性化配置
		if(success) {
			context.info("开始配置项目");
			context.info("====未实现====");
			// TODO: 
			// 1. dojo 项目
			// 2. spring boot 项目
		}


		// 开始构建 dojo 项目
		// 1. 安装依赖
		if(success) {
			context.info("开始执行 cnpm install 命令");
			NpmInstallTask npmInstallTask = new NpmInstallTask(context);
			success = npmInstallTask.run().isPresent();
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
			saveAppReleaseInfo(project, releaseTask, context);
		}
		
		// 更新发布任务的状态
		releaseTask.setEndTime(LocalDateTime.now());
		releaseTask.setReleaseResult(success ? ReleaseResult.PASSED : ReleaseResult.FAILED);
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
	}

	@Transactional
	private void saveAppReleaseInfo(Project project, ProjectReleaseTask releaseTask, AppBuildContext context) {
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
			relation.setDependAppReleaseId(releaseTask.getJdkAppId());
			appReleaseRelationDao.save(relation);
			
			// 4. 存储 APP_RELEASE_FILE
			AppReleaseFile file = new AppReleaseFile();
			file.setAppReleaseId(appRelease.getId());
			file.setTargetOs(TargetOs.ANY);
			file.setArch(Arch.ANY);
			file.setFileName(context.getMavenInstallJar().getFileName().toString());
			file.setFilePath(context.getMavenInstallJar().toString());
			file.setCreateTime(LocalDateTime.now());
			file.setCreateUserId(releaseTask.getCreateUserId());
			return appReleaseFileDao.save(file);
		});
	}

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
	public void asyncBuild(Project project, ProjectReleaseTask releaseTask) {
		this.build(project, releaseTask);
	}
	
}
