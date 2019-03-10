package com.blocklang.release.controller;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.data.NewReleaseTaskParam;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.service.BuildService;
import com.blocklang.release.service.ProjectReleaseTaskService;
import com.blocklang.release.service.ProjectTagService;

import de.skuzzle.semantic.Version;

@EnableAsync
@RestController
public class ReleaseController {

	private static final Logger logger = LoggerFactory.getLogger(ReleaseController.class);
	
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ProjectTagService projectTagService;
	
	@Autowired
	private ProjectReleaseTaskService projectReleaseTaskService;
	
	@Autowired
	private BuildService buildService;
	
	@PostMapping("projects/{owner}/{projectName}/releases")
	public ResponseEntity<ProjectReleaseTask> newRelease(
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@Valid @RequestBody NewReleaseTaskParam releaseTask,
			BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			logger.error("发布信息不完整：{}", releaseTask);
			throw new InvalidRequestException(bindingResult);
		}
		
		if(!Version.isValidVersion(releaseTask.getVersion())) {
			logger.error("不是有效的语义化版本");
			bindingResult.rejectValue("version", "NotValid.version");
			throw new InvalidRequestException(bindingResult);
		}
		
		// 获取项目基本信息
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		// 获取项目的 git 标签信息
		projectTagService.find(project.getId(), releaseTask.getVersion()).ifPresent(projectTag -> {
			logger.error("版本号 {} 已被占用", releaseTask.getVersion());
			bindingResult.rejectValue("version", "Duplicated.version", new Object[] {
					releaseTask.getVersion()
			}, null);
			throw new InvalidRequestException(bindingResult);
		});
		
		// 获取最新的 git 标签信息
		projectTagService.findLatestTag(project.getId()).ifPresent(projectTag -> {
			Version previousVersion = Version.parseVersion(projectTag.getVersion(), true);
			Version currentVersion = Version.parseVersion(releaseTask.getVersion(), true);
			if(!currentVersion.isGreaterThan(previousVersion)) {
				logger.error("版本号应大于项目最新的版本号，但 {} 没有大于 {}", releaseTask.getVersion(), projectTag.getVersion());
				bindingResult.rejectValue("version", "NotValid.compareVersion", new Object[] {
						releaseTask.getVersion(), 
						projectTag.getVersion()}, null);
				throw new InvalidRequestException(bindingResult);
			}
		});

		Integer currentUserId = 1; // TODO: 从 session 中获取
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(project.getId());
		task.setVersion(releaseTask.getVersion());
		task.setTitle(releaseTask.getTitle());
		task.setDescription(releaseTask.getDescription());
		task.setJdkAppId(releaseTask.getJdkAppId());
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(currentUserId);
		ProjectReleaseTask savedTask = projectReleaseTaskService.save(task);
		
		// build 时间较长，放在异步方法中
		buildService.asyncBuild(project, savedTask);
		
		return new ResponseEntity<ProjectReleaseTask>(savedTask, HttpStatus.CREATED);
	}
	
	// 校验版本号是否被占用
	
	@GetMapping("projects/{owner}/{projectName}/releases")
	public ResponseEntity<List<ProjectReleaseTask>> listRelease(
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName) {
		
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		List<ProjectReleaseTask> releases = projectReleaseTaskService.findAllByProjectId(project.getId());
		return ResponseEntity.ok(releases);
		
	}
}
