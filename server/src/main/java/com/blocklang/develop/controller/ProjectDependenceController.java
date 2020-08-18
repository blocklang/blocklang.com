package com.blocklang.develop.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.data.AddDependenceParam;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.data.UpdateDependenceParam;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.ProjectBuildProfile;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.marketplace.constant.RepoType;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.blocklang.marketplace.service.ApiRepoService;
import com.blocklang.marketplace.service.ApiRepoVersionService;
import com.blocklang.marketplace.service.ComponentRepoService;
import com.blocklang.marketplace.service.ComponentRepoVersionService;

/**
 * 项目依赖
 * 
 * @author Zhengwei Jin
 *
 */
@RestController
public class ProjectDependenceController extends AbstractRepositoryController{

	private static final Logger logger = LoggerFactory.getLogger(ProjectDependenceController.class);
	
	@Autowired
	private RepositoryResourceService repositoryResourceService;
	@Autowired
	private ProjectDependenceService projectDependenceService;
	@Autowired
	private ComponentRepoService componentRepoService;
	@Autowired
	private ComponentRepoVersionService componentRepoVersionService;
	@Autowired
	private ApiRepoService apiRepoService;
	@Autowired
	private ApiRepoVersionService apiRepoVersionService;

	/**
	 * 获取项目的 dependence 资源信息。一个仓库中可存放多个项目，每个项目包含一个依赖配置。
	 * 
	 * 注意，这里重点是资源信息，不是依赖详情。
	 * 
	 * <p>
	 * 就如分开获取页面基本信息和页面模型一样，依赖本身也是项目资源的一种，
	 * 所以此方法用于获取项目依赖这个资源的基本信息，并不获取项目依赖项列表。
	 * 
	 * FIXME：如果是获取资源的基本信息，是不是可以将获取页面的基本信息、获取依赖的基本信息和获取 readme 等资源的基本信息，
	 * 放在一个方法中呢？
	 * </p>
	 * 
	 * @param principal
	 * @param owner 用户名
	 * @param repoName 仓库名
	 * @param projectName 项目名
	 * @return 依赖资源的基本信息
	 */
	@GetMapping("/repos/{owner}/{repoName}/{projectName}/dependency")
	public ResponseEntity<Map<String, Object>> getDependence(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String repoName,
			@PathVariable String projectName) {
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canRead(principal, repository).orElseThrow(NoAuthorizationException::new);
		RepositoryResource project = repositoryResourceService.findProject(repository.getId(), projectName).orElseThrow(ResourceNotFoundException::new);
		
		RepositoryResource resource = repositoryResourceService.findByKey(
				repository.getId(), 
				project.getId(), 
				RepositoryResourceType.DEPENDENCE, 
				project.getAppType(),
				RepositoryResource.DEPENDENCE_KEY).orElseThrow(ResourceNotFoundException::new);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("resourceId", resource.getId());
		result.put("pathes", RepositoryResourcePathUtil.combinePathes(Collections.singletonList(resource)));
		return ResponseEntity.ok(result);
	}

	// 新增的依赖库，默认依赖 master
	@PostMapping("/projects/{owner}/{projectName}/dependences")
	public ResponseEntity<ProjectDependenceData> addDependence(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String projectName,
			@Valid @RequestBody AddDependenceParam param,
			BindingResult bindingResult) {
		
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository project = repositoryService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, project).orElseThrow(NoAuthorizationException::new);
		ComponentRepo componentRepo = componentRepoService.findById(param.getComponentRepoId())
				.orElseThrow(ResourceNotFoundException::new);
		ComponentRepoVersion componentRepoVersion = componentRepoVersionService
				.findByComponentIdAndVersion(componentRepo.getId(), "master").orElse(null);
		// 不能重复添加依赖
		if(RepoType.IDE.equals(componentRepo.getRepoType())) {
			if(projectDependenceService.devDependenceExists(
					project.getId(), 
					param.getComponentRepoId())){
				logger.error("项目已依赖该组件仓库");
				bindingResult.rejectValue("componentRepoId", "Duplicated.dependence");
				throw new InvalidRequestException(bindingResult);
			}
		} else if(RepoType.PROD.equals(componentRepo.getRepoType())) {
			// 当前只支持一个默认 profile
			// 后续版本会考虑是否需要支持多个 profile
			// 默认依赖 master 分支中的内容
			if(componentRepoVersion != null) {
				if(projectDependenceService.buildDependenceExists(
						project.getId(), 
						componentRepo.getId(),
						componentRepoVersion.getAppType(),
						// 从客户端传过来的是 profileName
						ProjectBuildProfile.DEFAULT_PROFILE_NAME)){
					logger.error("项目已依赖该组件仓库");
					bindingResult.rejectValue("componentRepoId", "Duplicated.dependence");
					throw new InvalidRequestException(bindingResult);
				}
			}
		}
		
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		ProjectDependence savedProjectDependence = projectDependenceService.save(project.getId(), componentRepo, user.getId());
		
		if(componentRepoVersion == null) {
			logger.error("组件仓库 {0} 没有找到已发布的 master", componentRepo.getGitRepoUrl());
			throw new ResourceNotFoundException();
		}
		
		ApiRepoVersion apiRepoVersion = apiRepoVersionService.findById(componentRepoVersion.getApiRepoVersionId()).orElseThrow(() -> {
			logger.error("未找到 ID 为 {0} 的 API 仓库的版本", componentRepoVersion.getApiRepoVersionId());
			return new ResourceNotFoundException();
		});

		ApiRepo apiRepo = apiRepoService.findById(apiRepoVersion.getApiRepoId()).orElseThrow(() -> {
			logger.error("未找到 ID 为 {0} 的 API 仓库", apiRepoVersion.getApiRepoId());
			return new ResourceNotFoundException();
		});
		
		ProjectDependenceData result = new ProjectDependenceData(savedProjectDependence, componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
		return new ResponseEntity<ProjectDependenceData>(result, HttpStatus.CREATED);
	}
	
	@GetMapping("/projects/{owner}/{projectName}/dependences")
	public ResponseEntity<List<ProjectDependenceData>> listDependences(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String projectName) {
		Repository project = repositoryService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canRead(principal, project).orElseThrow(NoAuthorizationException::new);
		
		List<ProjectDependenceData> result = projectDependenceService.findProjectDependences(project.getId());
		return ResponseEntity.ok(result);
	}
	
	@DeleteMapping("/projects/{owner}/{projectName}/dependences/{dependenceId}")
	public ResponseEntity<?> deleteDependence(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String projectName,
			@PathVariable Integer dependenceId) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository project = repositoryService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, project).orElseThrow(NoAuthorizationException::new);
		
		// FIXME: 是否有必要添加 try，如果需要，则用测试用例确认。
		try {
			projectDependenceService.delete(dependenceId);
		}catch (EmptyResultDataAccessException e) {
			logger.warn("该依赖已不存在", e);
		}
		
		return ResponseEntity.noContent().build();
	}
	
	@PutMapping("/projects/{owner}/{projectName}/dependences/{dependenceId}")
	public ResponseEntity<ComponentRepoVersion> updateDependence(Principal principal,
			@PathVariable String owner,
			@PathVariable String projectName,
			@PathVariable Integer dependenceId,
			@RequestBody UpdateDependenceParam param) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository project = repositoryService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, project).orElseThrow(NoAuthorizationException::new);
		
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		ProjectDependence dependence = projectDependenceService.findById(dependenceId).orElseThrow(ResourceNotFoundException::new);
		dependence.setComponentRepoVersionId(param.getComponentRepoVersionId());
		dependence.setLastUpdateTime(LocalDateTime.now());
		dependence.setLastUpdateUserId(user.getId());
		projectDependenceService.update(dependence);
		ComponentRepoVersion result = componentRepoVersionService.findById(param.getComponentRepoVersionId()).orElseThrow(ResourceNotFoundException::new);
		// 因为这里只是版本更新，所以只返回组件仓库的版本信息
		return new ResponseEntity<ComponentRepoVersion>(result, HttpStatus.CREATED);
	}

}
