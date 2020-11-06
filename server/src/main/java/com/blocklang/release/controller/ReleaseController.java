package com.blocklang.release.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.eclipse.jgit.revwalk.RevCommit;
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

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.UserService;
import com.blocklang.core.util.LogFileReader;
import com.blocklang.develop.constant.BuildTarget;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.RepositoryPermissionService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.develop.service.RepositoryService;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.data.CheckReleaseVersionParam;
import com.blocklang.release.data.MiniProgramStore;
import com.blocklang.release.data.NewReleaseTaskParam;
import com.blocklang.release.model.ProjectRelease;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.model.RepositoryTag;
import com.blocklang.release.service.BuildService;
import com.blocklang.release.service.ProjectReleaseService;
import com.blocklang.release.service.ProjectReleaseTaskService;
import com.blocklang.release.service.RepositoryTagService;
import com.blocklang.release.task.AppBuildContext;

import de.skuzzle.semantic.Version;

@EnableAsync
@RestController
public class ReleaseController {

	private static final Logger logger = LoggerFactory.getLogger(ReleaseController.class);
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryPermissionService repositoryPermissionService;
	@Autowired
	private RepositoryResourceService repositoryResourceService;
	@Autowired
	private RepositoryTagService projectTagService;
	@Autowired
	private ProjectReleaseService projectReleaseService;
	@Autowired
	private ProjectReleaseTaskService projectReleaseTaskService;
	@Autowired
	private UserService userService;
	@Autowired
	private BuildService buildService;
	@Autowired
	private PropertyService propertyService;
	
	@PostMapping("/repos/{owner}/{repoName}/{projectName}/releases")
	public ResponseEntity<ProjectReleaseTask> newRelease(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("repoName") String repoName,
			@PathVariable("projectName") String projectName,
			@RequestBody NewReleaseTaskParam releaseTaskParam) {
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		RepositoryResource project = repositoryResourceService.findProject(repository.getId(), projectName).orElseThrow(ResourceNotFoundException::new);
		UserInfo currentUser = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		
		Optional<ProjectRelease> projectReleaseOption = projectReleaseService.findByRepositoryIdAndProjectIdAndVersionAndBuildTarget(repository.getId(), project.getId(), releaseTaskParam.getVersion(), BuildTarget.fromKey(releaseTaskParam.getBuildTarget()));
		// 判断是否已发布过，如果已发布过，则不再发布
		// 1. 获取项目目录的最新 commit id
		
		String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH, "");
		MiniProgramStore store = new MiniProgramStore(
				dataRootPath, 
				repository.getCreateUserName(), 
				repository.getName(), 
				project.getKey(),
				BuildTarget.fromKey(releaseTaskParam.getBuildTarget()),
				releaseTaskParam.getVersion());
		
		RevCommit latestCommit = GitUtils.getLatestCommit(store.getModelRepositoryDirectory(), project.getKey());
		if(latestCommit == null) {
			throw new ResourceNotFoundException();
		}
		ProjectRelease projectRelease = null;
		if(projectReleaseOption.isPresent()) {
			projectRelease = projectReleaseOption.get();
			if(projectRelease.getCommitId().equals(latestCommit.getName())) {
				// 没有最新要发布的内容，不启动发布流程
				return ResponseEntity.ok(new ProjectReleaseTask());
			}
		}
		
		// TODO: 如果项目正在发布，则不启动发布任务
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setRepositoryId(repository.getId());
		task.setProjectId(project.getId());
		task.setVersion(releaseTaskParam.getVersion());
		task.setTitle(releaseTaskParam.getTitle());
		task.setDescription(releaseTaskParam.getDescription());
		task.setJdkReleaseId(releaseTaskParam.getJdkReleaseId());
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(currentUser.getId());
		task.setCommitId(latestCommit.getName());
		task.setBuildTarget(BuildTarget.fromKey(releaseTaskParam.getBuildTarget()));
		ProjectReleaseTask savedTask = projectReleaseTaskService.save(task);
		
		buildService.buildProject(repository, project, savedTask, store);
		return new ResponseEntity<ProjectReleaseTask>(savedTask, HttpStatus.CREATED);
	}
	
	@Deprecated
	@PostMapping("/projects/{owner}/{projectName}/releases")
	public ResponseEntity<ProjectReleaseTask> newRelease(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@Valid @RequestBody NewReleaseTaskParam releaseTask,
			BindingResult bindingResult) {
		
		String version = releaseTask.getVersion();
		
		Validator validator = new Validator();
		validator.validateReleaseVersion(principal, owner, projectName, version, bindingResult);

		Repository project = validator.project;
		
		UserInfo currentUser = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		Integer currentUserId = currentUser.getId();
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(project.getId());
		task.setVersion(releaseTask.getVersion());
		task.setTitle(releaseTask.getTitle());
		task.setDescription(releaseTask.getDescription());
		task.setJdkReleaseId(releaseTask.getJdkReleaseId());
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(currentUserId);
		ProjectReleaseTask savedTask = projectReleaseTaskService.save(task);
		
		// build 时间较长，放在异步方法中
		buildService.asyncBuild(project, savedTask);
		
		return new ResponseEntity<ProjectReleaseTask>(savedTask, HttpStatus.CREATED);
	}
	
	// 校验版本号
	@PostMapping("/projects/{owner}/{projectName}/releases/check-version")
	public ResponseEntity<Map<String, String>> checkVersion(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@Valid @RequestBody CheckReleaseVersionParam param, 
			BindingResult bindingResult) {
		String version = param.getVersion();
		Validator validator = new Validator();
		validator.validateReleaseVersion(principal, owner, projectName, version, bindingResult);
		return ResponseEntity.ok(new HashMap<String, String>());
	}

	private class Validator {
		private Repository project;
		private void validateReleaseVersion(
				Principal principal, 
				String owner, 
				String projectName,
				String version,
				BindingResult bindingResult) {
			if(principal == null) {
				throw new NoAuthorizationException();
			}
			
			if(bindingResult.hasErrors()) {
				logger.error("版本号不能为空");
				throw new InvalidRequestException(bindingResult);
			}
			
			if(!Version.isValidVersion(version)) {
				logger.error("不是有效的语义化版本");
				bindingResult.rejectValue("version", "NotValid.version");
				throw new InvalidRequestException(bindingResult);
			}
			
			// 获取项目基本信息
			project = repositoryService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
			
			Optional<RepositoryTag> latestTagOption = projectTagService.findLatestTag(project.getId());
			// 获取项目的 git 标签信息
			projectTagService.find(project.getId(), version).ifPresent(projectTag -> {
				logger.error("版本号 {} 已被占用", version);
				// 如果发现版本号已被占用，则肯定有最新版本号，所以不需要做是否为空判断
				RepositoryTag latestTag = latestTagOption.get();
				bindingResult.rejectValue("version", "Duplicated.version", new Object[] {
						version,
						latestTag.getVersion()
				}, null);
				throw new InvalidRequestException(bindingResult);
			});
			
			// 获取最新的 git 标签信息
			latestTagOption.ifPresent(projectTag -> {
				Version previousVersion = Version.parseVersion(projectTag.getVersion(), true);
				Version currentVersion = Version.parseVersion(version, true);
				if(!currentVersion.isGreaterThan(previousVersion)) {
					logger.error("版本号应大于项目最新的版本号，但 {} 没有大于 {}", version, projectTag.getVersion());
					bindingResult.rejectValue("version", "NotValid.compareVersion", new Object[] {
							projectTag.getVersion()}, null);
					throw new InvalidRequestException(bindingResult);
				}
			});
		}
	}
	
	@GetMapping("/projects/{owner}/{projectName}/releases")
	public ResponseEntity<List<ProjectReleaseTask>> listRelease(
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName) {
		
		Repository project = repositoryService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		List<ProjectReleaseTask> releases = projectReleaseTaskService.findAllByProjectId(project.getId());
		return ResponseEntity.ok(releases);
	}
	
	@GetMapping("/projects/{owner}/{projectName}/stats/releases")
	public ResponseEntity<Map<String, Long>> getReleaseCount(
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName) {
		
		Repository project = repositoryService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		Long count = projectReleaseTaskService.count(project.getId());
		Map<String, Long> result = new HashMap<String, Long>();
		result.put("total", count);
		return ResponseEntity.ok(result);
	}
	
	@GetMapping("/projects/{owner}/{projectName}/releases/{version}")
	public ResponseEntity<ProjectReleaseTask> getRelease(
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@PathVariable("version") String version) {

		Repository project = repositoryService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		return projectReleaseTaskService.findByProjectIdAndVersion(project.getId(), version).map((task) -> {
			return ResponseEntity.ok(task);
		}).orElseThrow(ResourceNotFoundException::new);
		
	}
	
	@GetMapping("/projects/{owner}/{projectName}/releases/{version}/log")
	public ResponseEntity<List<String>> getReleaseLog(
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@PathVariable("version") String version) {

		Repository project = repositoryService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		ProjectReleaseTask task = projectReleaseTaskService.findByProjectIdAndVersion(project.getId(), version).orElseThrow(ResourceNotFoundException::new);
		
		Path logFilePath = null;
		try {
			String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).get();
			logFilePath = new AppBuildContext.LogPathBuilder()
					.setDataRootPath(dataRootPath)
					.setOwner(owner)
					.setRepoName(projectName)
					.setLogFileName(task.getLogFileName())
					.build()
					.getLogFilePath();
		} catch (IOException e) {
			logger.warn("获取日志文件失败", e);
		}
		
		return ResponseEntity.ok(LogFileReader.readAllLines(logFilePath));
	}
}
