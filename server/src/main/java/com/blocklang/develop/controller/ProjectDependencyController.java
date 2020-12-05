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

import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.develop.constant.RepoBranchName;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.data.AddDependencyParam;
import com.blocklang.develop.data.ProjectDependencyData;
import com.blocklang.develop.data.UpdateDependencyParam;
import com.blocklang.develop.model.ProjectDependency;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.ProjectDependencyService;
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
public class ProjectDependencyController extends AbstractRepositoryController{

	private static final Logger logger = LoggerFactory.getLogger(ProjectDependencyController.class);
	
	@Autowired
	private RepositoryResourceService repositoryResourceService;
	@Autowired
	private ProjectDependencyService projectDependencyService;
	@Autowired
	private ComponentRepoService componentRepoService;
	@Autowired
	private ComponentRepoVersionService componentRepoVersionService;
	@Autowired
	private ApiRepoService apiRepoService;
	@Autowired
	private ApiRepoVersionService apiRepoVersionService;

	/**
	 * 获取项目的 dependency 资源信息。一个仓库中可存放多个项目，每个项目包含一个依赖配置。
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
	public ResponseEntity<Map<String, Object>> getDependency(
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
				RepositoryResourceType.DEPENDENCY, 
				project.getAppType(),
				RepositoryResource.DEPENDENCY_KEY).orElseThrow(ResourceNotFoundException::new);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("resourceId", resource.getId());
		result.put("pathes", RepositoryResourcePathUtil.combinePathes(Collections.singletonList(resource)));
		return ResponseEntity.ok(result);
	}

	// 新增项目依赖的组件库库，默认依赖组件库的 master 分支
	@PostMapping("/repos/{owner}/{repoName}/{projectName}/dependencies")
	public ResponseEntity<ProjectDependencyData> addDependency(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String repoName,
			@PathVariable String projectName,
			@Valid @RequestBody AddDependencyParam param,
			BindingResult bindingResult) {
		
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		RepositoryResource project = repositoryResourceService.findProject(repository.getId(), projectName).orElseThrow(ResourceNotFoundException::new);
		
		ComponentRepo componentRepo = componentRepoService.findById(param.getComponentRepoId()).orElseThrow(ResourceNotFoundException::new);
		// 默认依赖 master 分支
		ComponentRepoVersion componentRepoVersion = componentRepoVersionService
				.findByComponentIdAndVersion(componentRepo.getId(), RepoBranchName.MASTER)
				.orElseThrow(() -> {
					logger.error("组件仓库 {0} 没有找到 {1} 分支", componentRepo.getGitRepoUrl(), RepoBranchName.MASTER);
					throw new ResourceNotFoundException();
				});
		// 不能重复添加依赖
		if(RepoType.IDE.equals(componentRepo.getRepoType())) {
			if(projectDependencyService.devDependencyExists(project.getId(), param.getComponentRepoId())){
				logger.error("项目已依赖该组件仓库");
				bindingResult.rejectValue("componentRepoId", "Duplicated.dependency");
				throw new InvalidRequestException(bindingResult);
			}
		} else if(RepoType.PROD.equals(componentRepo.getRepoType())) {
			// 当前只支持一个默认 profile
			// 后续版本会考虑是否需要支持多个 profile
			// 默认依赖 master 分支中的内容
			if(componentRepoVersion != null) {
				if(projectDependencyService.buildDependencyExists(
						project.getId(), 
						// 默认的 profileName 为 default
						param.getBuildProfileId(),
						param.getComponentRepoId())){
					logger.error("项目已依赖该组件仓库");
					bindingResult.rejectValue("componentRepoId", "Duplicated.dependency");
					throw new InvalidRequestException(bindingResult);
				}
			}
		}
		
		ApiRepoVersion apiRepoVersion = apiRepoVersionService.findById(componentRepoVersion.getApiRepoVersionId()).orElseThrow(() -> {
			logger.error("未找到 ID 为 {0} 的 API 仓库的版本", componentRepoVersion.getApiRepoVersionId());
			return new ResourceNotFoundException();
		});

		ApiRepo apiRepo = apiRepoService.findById(apiRepoVersion.getApiRepoId()).orElseThrow(() -> {
			logger.error("未找到 ID 为 {0} 的 API 仓库", apiRepoVersion.getApiRepoId());
			return new ResourceNotFoundException();
		});
		
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		
		ProjectDependency projectDependency = new ProjectDependency();
		projectDependency.setRepositoryId(repository.getId());
		projectDependency.setProjectId(project.getId());
		projectDependency.setComponentRepoVersionId(componentRepoVersion.getId());
		projectDependency.setProfileId(param.getBuildProfileId());
		projectDependency.setCreateUserId(user.getId());
		projectDependency.setCreateTime(LocalDateTime.now());
		
		ProjectDependency savedProjectDependency = projectDependencyService.save(repository, project, projectDependency);
		ProjectDependencyData result = new ProjectDependencyData(savedProjectDependency, componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
		return new ResponseEntity<ProjectDependencyData>(result, HttpStatus.CREATED);
	}
	
	@GetMapping("/repos/{owner}/{repoName}/{projectName}/dependencies")
	public ResponseEntity<List<ProjectDependencyData>> listDependencies(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String repoName,
			@PathVariable String projectName) {
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canRead(principal, repository).orElseThrow(NoAuthorizationException::new);
		RepositoryResource project = repositoryResourceService.findProject(repository.getId(), projectName).orElseThrow(ResourceNotFoundException::new);
		
		List<ProjectDependencyData> result = projectDependencyService.findAllConfigDependencies(project.getId());
		// 获取标准库
		result.addAll(projectDependencyService.findStdDevDependencies(project.getAppType(), project.getDeviceType()));
		result.addAll(projectDependencyService.findStdBuildDependencies(project.getAppType()));
		
		return ResponseEntity.ok(result);
	}
	
	@DeleteMapping("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}")
	public ResponseEntity<?> deleteDependency(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String repoName,
			@PathVariable String projectName,
			@PathVariable Integer dependencyId) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository repository = repositoryService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		RepositoryResource project = repositoryResourceService.findProject(repository.getId(), projectName).orElseThrow(ResourceNotFoundException::new);
		
		// FIXME: 是否有必要添加 try，如果需要，则用测试用例确认。
		try {
			projectDependencyService.delete(repository, project, dependencyId);
		}catch (EmptyResultDataAccessException e) {
			logger.warn("该依赖已不存在", e);
		}
		
		return ResponseEntity.noContent().build();
	}
	
	@PutMapping("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}")
	public ResponseEntity<ComponentRepoVersion> updateDependency(Principal principal,
			@PathVariable String owner,
			@PathVariable String repoName,
			@PathVariable String projectName,
			@PathVariable Integer dependencyId,
			@RequestBody UpdateDependencyParam param) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		RepositoryResource project = repositoryResourceService.findProject(repository.getId(), projectName).orElseThrow(ResourceNotFoundException::new);
		
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		ProjectDependency dependency = projectDependencyService.findById(dependencyId).orElseThrow(ResourceNotFoundException::new);
		dependency.setComponentRepoVersionId(param.getComponentRepoVersionId());
		dependency.setLastUpdateTime(LocalDateTime.now());
		dependency.setLastUpdateUserId(user.getId());
		projectDependencyService.update(repository, project, dependency);
		ComponentRepoVersion result = componentRepoVersionService.findById(param.getComponentRepoVersionId()).orElseThrow(ResourceNotFoundException::new);
		// 因为这里只是版本更新，所以只返回组件仓库的版本信息
		return new ResponseEntity<ComponentRepoVersion>(result, HttpStatus.CREATED);
	}

}
