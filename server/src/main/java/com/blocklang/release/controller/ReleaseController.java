package com.blocklang.release.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.develop.model.Project;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.exception.InvalidRequestException;
import com.blocklang.exception.ResourceNotFoundException;
import com.blocklang.release.constant.BuildResult;
import com.blocklang.release.data.NewReleaseParam;
import com.blocklang.release.model.ProjectBuild;
import com.blocklang.release.model.ProjectTag;
import com.blocklang.release.service.BuildToolService;
import com.blocklang.release.service.GitToolService;
import com.blocklang.release.service.ProjectBuildService;
import com.blocklang.release.service.ProjectTagService;
import com.blocklang.release.task.AppBuildContext;

@RestController
public class ReleaseController {

	private static final Logger logger = LoggerFactory.getLogger(ReleaseController.class);
	
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ProjectTagService projectTagService;
	
	@Autowired
	private ProjectBuildService projectBuildService;
	
	@Autowired
	private GitToolService gitService;
	
	@Autowired
	private BuildToolService buildToolService;
	
	@PostMapping("projects/{owner}/{projectName}/releases")
	public ResponseEntity<?> newRelease(
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@Valid @RequestBody NewReleaseParam release,
			BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			logger.error("发布信息不完整：{}", release);
			throw new InvalidRequestException(bindingResult);
		}
		
		// 获取项目基本信息
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		// 获取项目的 git 仓库信息
		projectTagService.find(project.getId(), release.getVersion()).ifPresent(projectTag -> {
			logger.error("版本号 {} 已被占用", release.getVersion());
			bindingResult.rejectValue("version", "Duplicated.version", new Object[] {
					release.getVersion()
			}, null);
			throw new InvalidRequestException(bindingResult);
		});
		
		logger.info("==============================");
		logger.info("开始 build @{}/{} 项目", owner, projectName);
		String projectsRootPath = "E:/data/blocklang"; // TODO: 从系统参数中读取
		String mavenRootPath = "c:/Users/Administrator/.m2"; // TODO: 从系统参数中读取
		
		AppBuildContext appBuildContext = new AppBuildContext(projectsRootPath, mavenRootPath, owner, projectName, release.getVersion());
		// 为 git 仓库打标签
		logger.info("开始为 git 仓库打标签");
		String tagId = gitService.tag(appBuildContext).orElseThrow(() -> {
			logger.error("为 Git 仓库添加附注标签失败");
			bindingResult.reject("Error.gitTag");
			throw new InvalidRequestException(bindingResult);
		});
		logger.info("为 Git 仓库添加附注标签成功");
		
		// build 是一个完整的过程，过程中的任何一个环节出错，则该过程就出错
		// 但不管最终是成功还是失败都是一个结果，而不需要为不同的结果设置不同的状态码
		// 不论成功或失败，状态码都应该是一个。
		
		// 注意：
		// 后续的数据库存储操作，因为都隶属于孤立的流程节点，
		// 每个节点都有保存失败的情况，不需要放在同一个数据库事务中。
		
		// 标签添加成功后，在数据库中记录标签信息
		ProjectTag projectTag = new ProjectTag();
		projectTag.setProjectId(project.getId());
		projectTag.setVersion(release.getVersion());
		projectTag.setGitTagId(tagId);
		projectTag.setCreateTime(LocalDateTime.now());
		projectTag.setCreateUserId(1); // TODO: 从 session 中获取信息
		Integer projectTagId = projectTagService.save(projectTag).getId();
		
		// 开始 build，这里存储构建开始信息
		ProjectBuild projectBuild = new ProjectBuild();
		projectBuild.setCreateTime(LocalDateTime.now());
		projectBuild.setCreateUserId(1); // TODO: 从 session 中获取信息
		projectBuild.setProjectTagId(projectTagId);
		projectBuild.setStartTime(LocalDateTime.now());
		projectBuild.setBuildResult(BuildResult.STARTED);
		ProjectBuild savedProjectBuild = projectBuildService.save(projectBuild);
		
		// TODO: 考虑将代码放在 controller 中还是 service 中好
		// 开始对项目进行个性化配置
		// 1. dojo 项目
		// 2. spring boot 项目
		
		boolean buildSuccess = false;
		// 开始构建 dojo 项目
		// 1. 安装依赖
		buildSuccess = buildToolService.runNpmInstall(appBuildContext);
		// 2. 构建 dojo 项目
		if(buildSuccess) {
			buildSuccess = buildToolService.runDojoBuild(appBuildContext);
		}
		
		// 3. 将发布的 dojo 代码复制到 spring boot 的 static 和 templates 文件夹中
		if(buildSuccess) {
			buildSuccess = buildToolService.copyDojoDistToSpringBoot(appBuildContext);
		}
		
		// 开始构建 spring boot 项目
		if(buildSuccess) {
			buildSuccess = buildToolService.runMavenInstall(appBuildContext);
		}
		
		// 修改构建状态
		savedProjectBuild.setEndTime(LocalDateTime.now());
		savedProjectBuild.setBuildResult(buildSuccess ? BuildResult.PASSED : BuildResult.FAILED);
		projectBuildService.update(savedProjectBuild);
		
		// 构建成功后，存储 APP 发布信息
		
		
		Map<String, Object> data = new HashMap<String, Object>();
		return ResponseEntity.ok(data);
		
	}
	
	// 校验版本号是否被占用
}
