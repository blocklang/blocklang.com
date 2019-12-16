package com.blocklang.develop.controller;

import java.security.Principal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.blocklang.core.util.NumberUtil;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.AppNames;
import com.blocklang.develop.data.CheckProjectNameParam;
import com.blocklang.develop.data.DeploySetting;
import com.blocklang.develop.data.GitCommitInfo;
import com.blocklang.develop.data.NewProjectParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectFile;
import com.blocklang.develop.service.ProjectDeployService;
import com.blocklang.develop.service.ProjectFileService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.TargetOs;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.service.AppReleaseService;
import com.nimbusds.oauth2.sdk.util.StringUtils;

@RestController
public class ProjectController extends AbstractProjectController{

	private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

	@Autowired
	private ProjectResourceService projectResourceService;
	@Autowired
	private ProjectFileService projectFileService;
	@Autowired
	private ProjectDeployService projectDeployService;
	@Autowired
	private UserService userService;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private AppReleaseService appReleaseService;
	
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
	
	@GetMapping("/projects/{owner}/{projectName}/readme")
	public ResponseEntity<String> getReadme(
			Principal user,
			@PathVariable String owner,
			@PathVariable String projectName) {
		
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		projectPermissionService.canRead(user, project).orElseThrow(NoAuthorizationException::new);
		
		String readme = projectFileService.findReadme(project.getId()).map(ProjectFile::getContent).orElse("");
		return ResponseEntity.ok(readme);
	}
	
	@GetMapping("/user/projects")
	public ResponseEntity<List<Project>> getYourProjects(Principal principal) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		return ResponseEntity.ok(projectService.findCanAccessProjectsByUserId(user.getId()));
	}

	@GetMapping("/projects/{owner}/{projectName}")
	public ResponseEntity<Project> getProject(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String projectName) {
		
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		projectPermissionService.canRead(principal, project).orElseThrow(NoAuthorizationException::new);
		AccessLevel accessLevel = projectPermissionService.findTopestPermission(principal, project);
		project.setAccessLevel(accessLevel);
		return ResponseEntity.ok(project);
	}
	
	@GetMapping("/projects/{owner}/{projectName}/latest-commit/{parentId}")
	public ResponseEntity<GitCommitInfo> getLatestCommit(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String projectName,
			@PathVariable String parentId) {
		
		Integer resourceId = NumberUtil.toInt(parentId).orElseThrow(ResourceNotFoundException::new);
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		projectPermissionService.canRead(principal, project).orElseThrow(NoAuthorizationException::new);
		
		String filePath = String.join("/", projectResourceService.findParentPathes(resourceId));
		project.setCreateUserName(owner);
		GitCommitInfo commitInfo = projectService.findLatestCommitInfo(project, filePath).orElse(null);
		
		return ResponseEntity.ok(commitInfo);
	}
	
	@GetMapping("/projects/{owner}/{projectName}/deploy_setting")
	public ResponseEntity<DeploySetting> getDeploySetting(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String projectName) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		projectPermissionService.canRead(principal, project).orElseThrow(NoAuthorizationException::new);
		
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		String url = propertyService.findStringValue(CmPropKey.INSTALL_API_ROOT_URL, "https://blocklang.com");
		// blocklang-installer 软件的下载链接，目前只支持64位：
		String downloadInstallerUrlPattern = "/apps?appName={0}&version={1}&targetOs={2}&arch={3}";
		
		DeploySetting result = projectDeployService.findOrCreate(project.getId(), user.getId()).map(deploy -> {
			
			String installerLinuxUrl = null;
			String installerWindowsUrl = null;
			String latestInstallerVersion = appReleaseService
					.findLatestReleaseAppByAppName(AppNames.BLOCKLANG_INSTALLER)
					.map(AppRelease::getVersion)
					.orElse(null);
			if(StringUtils.isNotBlank(latestInstallerVersion)) {
				installerLinuxUrl = MessageFormat.format(downloadInstallerUrlPattern, 
						AppNames.BLOCKLANG_INSTALLER, 
						latestInstallerVersion,
						TargetOs.LINUX.getValue().toLowerCase(),
						Arch.X86_64.getValue().toLowerCase());
				installerWindowsUrl = MessageFormat.format(downloadInstallerUrlPattern, 
						AppNames.BLOCKLANG_INSTALLER, 
						latestInstallerVersion,
						TargetOs.WINDOWS.getValue().toLowerCase(),
						Arch.X86_64.getValue().toLowerCase());
			}
			
			DeploySetting deploySetting = new DeploySetting();
			deploySetting.setId(deploy.getId());
			deploySetting.setUserId(deploy.getUserId());
			deploySetting.setProjectId(deploy.getProjectId());
			deploySetting.setRegistrationToken(deploy.getRegistrationToken());
			deploySetting.setDeployState(deploy.getDeployState().getKey());
			
			deploySetting.setUrl(url);
			deploySetting.setInstallerLinuxUrl(installerLinuxUrl);
			deploySetting.setInstallerWindowsUrl(installerWindowsUrl);
			
			return deploySetting;
		}).orElse(new DeploySetting());
		
		return ResponseEntity.ok(result);
	}
}
