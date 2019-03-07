package com.blocklang.develop.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.UserService;
import com.blocklang.develop.data.CheckProjectNameParam;
import com.blocklang.develop.data.DeploySetting;
import com.blocklang.develop.data.GitCommitInfo;
import com.blocklang.develop.data.NewProjectParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectDeployService;
import com.blocklang.develop.service.ProjectFileService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;

@RestController
public class ProjectController {

	private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectResourceService projectResourceService;
	@Autowired
	private ProjectFileService projectFileService;
	@Autowired
	private ProjectDeployService projectDeployService;
	@Autowired
	private UserService userService;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private PropertyService propertyService;
	
	@PostMapping("/projects/check-name")
	public ResponseEntity<Map<String, Object>> checkProjectName(
			Principal principal,
			@Valid @RequestBody CheckProjectNameParam param, 
			BindingResult bindingResult) {

		validateOwner(principal, param.getOwner());
		validateProjectName(bindingResult, param);
		return new ResponseEntity<Map<String,Object>>(new HashMap<String,Object>(), HttpStatus.OK);
	}
	
	@PostMapping("/projects")
	public ResponseEntity<Project> newProject(
			Principal principal, 
			@Valid @RequestBody NewProjectParam param, 
			BindingResult bindingResult) {
		
		validateOwner(principal, param.getOwner());
		validateProjectName(bindingResult, param);
		
		Project savedProject = userService.findByLoginName(param.getOwner()).map(user -> {
			Project project = new Project();
			project.setName(param.getName());
			project.setDescription(param.getDescription());
			project.setIsPublic(param.getIsPublic());
			project.setCreateUserId(user.getId());
			project.setCreateTime(LocalDateTime.now());
			project.setLastActiveTime(LocalDateTime.now());
			
			project.setCreateUserName(user.getLoginName());
			
			return projectService.create(user, project);
		}).orElse(null);
		
		return new ResponseEntity<Project>(savedProject, HttpStatus.CREATED);
	}

	private void validateOwner(Principal principal, String owner) {
		if(principal == null || !principal.getName().equals(owner)) {
			throw new NoAuthorizationException();
		}
	}
	
	private void validateProjectName(BindingResult bindingResult, CheckProjectNameParam param) {
		if(bindingResult.hasErrors()) {
			logger.error("项目名称校验未通过。");
			throw new InvalidRequestException(bindingResult);
		}
		
		projectService.find(param.getOwner(), param.getName()).ifPresent((project) -> {
			logger.error("项目名 {} 已被占用", param.getName());
			bindingResult.rejectValue("name", "Duplicated.projectName", new Object[] {
				param.getOwner(), param.getName()
			}, null);
			throw new InvalidRequestException(bindingResult);
		});
	}

	@GetMapping("/projects/{owner}/{projectName}/tree/{pathId}")
	public ResponseEntity<List<ProjectResource>> getTree(
			Principal user,
			@PathVariable String owner,
			@PathVariable String projectName,
			@PathVariable String pathId) {
		
		Integer resourceId;
		try {
			resourceId = Integer.valueOf(pathId);
		} catch (NumberFormatException e) {
			logger.error("无法将 ‘" + pathId + "’ 转换为数字", e);
			throw new ResourceNotFoundException();
		}
		
		return projectService.find(owner, projectName).map((project) -> {
			if(!project.getIsPublic()) {
				// 1. 用户未登录时不能访问私有项目
				// 2. 用户虽然登录，但是不是项目的拥有者且没有访问权限，则不能访问
				if((user == null) || (user!= null && !owner.equals(user.getName()))) {
					throw new ResourceNotFoundException();
				}
			}
			project.setCreateUserName(owner);
			List<ProjectResource> tree = projectResourceService.findChildren(project, resourceId);
			tree.forEach(projectResource -> {
				projectResource.setMessageSource(messageSource);
			});
			return ResponseEntity.ok(tree);
		}).orElseThrow(ResourceNotFoundException::new);
	}
	
	@GetMapping("/projects/{owner}/{projectName}/readme")
	public ResponseEntity<String> getReadme(
			Principal user,
			@PathVariable String owner,
			@PathVariable String projectName) {
		
		return projectService.find(owner, projectName).flatMap(project -> {
			
			if(!project.getIsPublic()) {
				// 1. 用户未登录时不能访问私有项目
				// 2. 用户虽然登录，但是不是项目的拥有者且没有访问权限，则不能访问
				if((user == null) || (user!= null && !owner.equals(user.getName()))) {
					throw new ResourceNotFoundException();
				}
			}
			
			return projectFileService.findReadme(project.getId());
		}).map(projectFile -> {
			return ResponseEntity.ok(projectFile.getContent());
		}).orElseThrow(ResourceNotFoundException::new);
	}
	
	@GetMapping("/user/projects")
	public ResponseEntity<List<Project>> getYourProjects(Principal principal) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		return userService.findByLoginName(principal.getName()).map(user -> {
			return ResponseEntity.ok(projectService.findCanAccessProjectsByUserId(user.getId()));
		}).orElseThrow(NoAuthorizationException::new);
	}

	@GetMapping("/projects/{owner}/{projectName}")
	public ResponseEntity<Project> getProject(
			@PathVariable String owner,
			@PathVariable String projectName) {
		return projectService.find(owner, projectName).map(project -> {
			return ResponseEntity.ok(project);
		}).orElseThrow(ResourceNotFoundException::new);
	}
	
	@GetMapping("/projects/{owner}/{projectName}/latest-commit/{pathId}")
	public ResponseEntity<GitCommitInfo> getLatestCommit(
			@PathVariable String owner,
			@PathVariable String projectName,
			@PathVariable String pathId) {
		
		Integer resourceId;
		try {
			resourceId = Integer.valueOf(pathId);
		} catch (NumberFormatException e) {
			logger.error("无法将 ‘" + pathId + "’ 转换为数字", e);
			throw new ResourceNotFoundException();
		}
		
		return projectService.find(owner, projectName).flatMap(project -> {
			String filePath = projectResourceService.findParentPath(resourceId);
			project.setCreateUserName(owner);
			return projectService.findLatestCommitInfo(project, filePath);
		}).map(commitInfo -> {
			return ResponseEntity.ok(commitInfo);
		}).orElseThrow(ResourceNotFoundException::new);
	}
	
	@GetMapping("/projects/{owner}/{projectName}/deploy_setting")
	public ResponseEntity<DeploySetting> getDeploySetting(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String projectName) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		
		String url = propertyService.findStringValue(CmPropKey.INSTALL_API_ROOT_URL, "https://blocklang.com");
		String installerLinuxUrl = propertyService.findStringValue(CmPropKey.INSTALLER_LINUX_URL).orElseThrow(() -> {
			logger.error("请在系统参数中配置 Linux 版安装器的下载地址");
			return new ResourceNotFoundException();
		});
		String installerWindowsUrl = propertyService.findStringValue(CmPropKey.INSTALLER_WINDOWS_URL).orElseThrow(() -> {
			logger.error("请在系统参数中配置 Windows 版安装器的下载地址");
			return new ResourceNotFoundException();
		});
		
		return projectService.find(owner, projectName).flatMap(project -> {
			return projectDeployService.findOrCreate(project.getId(), user.getId());
		}).map(deploy -> {
			DeploySetting deploySetting = new DeploySetting();
			deploySetting.setId(deploy.getId());
			deploySetting.setUserId(deploy.getUserId());
			deploySetting.setProjectId(deploy.getProjectId());
			deploySetting.setRegistrationToken(deploy.getRegistrationToken());
			deploySetting.setDeployState(deploy.getDeployState().getKey());
			
			deploySetting.setUrl(url);
			deploySetting.setInstallerLinuxUrl(installerLinuxUrl);
			deploySetting.setInstallerWindowsUrl(installerWindowsUrl);
			
			return ResponseEntity.ok(deploySetting);
		}).orElse(ResponseEntity.ok(new DeploySetting()));
	}
}
