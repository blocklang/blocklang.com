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
import com.blocklang.develop.data.CheckRepositoryNameParam;
import com.blocklang.develop.data.DeploySetting;
import com.blocklang.develop.data.GitCommitInfo;
import com.blocklang.develop.data.NewRepositoryParam;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryFile;
import com.blocklang.develop.service.ProjectDeployService;
import com.blocklang.develop.service.RepositoryFileService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.TargetOs;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.service.AppReleaseService;
import com.nimbusds.oauth2.sdk.util.StringUtils;

@RestController
public class RepositoryController extends AbstractRepositoryController{

	private static final Logger logger = LoggerFactory.getLogger(RepositoryController.class);

	@Autowired
	private RepositoryResourceService repositoryResourceService;
	@Autowired
	private RepositoryFileService repositoryFileService;
	@Autowired
	private ProjectDeployService projectDeployService;
	@Autowired
	private UserService userService;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private AppReleaseService appReleaseService;
	
	@PostMapping("/repos/check-name")
	public ResponseEntity<Map<String, Object>> checkRepositoryName(
			Principal principal,
			@Valid @RequestBody CheckRepositoryNameParam param, 
			BindingResult bindingResult) {

		validateOwner(principal, param.getOwner());
		validateRepositoryName(bindingResult, param);
		return new ResponseEntity<Map<String,Object>>(new HashMap<String,Object>(), HttpStatus.OK);
	}
	
	@PostMapping("/repos")
	public ResponseEntity<Repository> newRepository(
			Principal principal, 
			@Valid @RequestBody NewRepositoryParam param, 
			BindingResult bindingResult) {
		
		validateOwner(principal, param.getOwner());
		validateRepositoryName(bindingResult, param);
		
		Repository savedRepository = userService.findByLoginName(param.getOwner()).map(user -> {
			Repository repository = new Repository();
			repository.setName(param.getName());
			repository.setDescription(param.getDescription());
			repository.setIsPublic(param.getIsPublic());
			repository.setCreateUserId(user.getId());
			repository.setCreateTime(LocalDateTime.now());
			repository.setLastActiveTime(LocalDateTime.now());
			repository.setCreateUserName(user.getLoginName());
			
			return repositoryService.createRepository(user, repository);
		}).orElse(null);
		
		return new ResponseEntity<Repository>(savedRepository, HttpStatus.CREATED);
	}

	private void validateOwner(Principal principal, String owner) {
		if(principal == null || !principal.getName().equals(owner)) {
			throw new NoAuthorizationException();
		}
	}
	
	private void validateRepositoryName(BindingResult bindingResult, CheckRepositoryNameParam param) {
		if(bindingResult.hasErrors()) {
			logger.error("仓库名校验未通过。");
			throw new InvalidRequestException(bindingResult);
		}
		
		repositoryService.find(param.getOwner(), param.getName()).ifPresent((repository) -> {
			logger.error("仓库名 {} 已被占用", param.getName());
			bindingResult.rejectValue("name", "Duplicated.repositoryName", new Object[] {
				param.getOwner(), param.getName()
			}, null);
			throw new InvalidRequestException(bindingResult);
		});
	}
	
	@GetMapping("/repos/{owner}/{repoName}/readme")
	public ResponseEntity<String> getReadme(
			Principal user,
			@PathVariable String owner,
			@PathVariable String repoName) {
		
		Repository project = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canRead(user, project).orElseThrow(NoAuthorizationException::new);
		
		String readme = repositoryFileService.findReadme(project.getId()).map(RepositoryFile::getContent).orElse("");
		return ResponseEntity.ok(readme);
	}
	
	@GetMapping("/user/repos")
	public ResponseEntity<List<Repository>> getYourRepos(Principal principal) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		return ResponseEntity.ok(repositoryService.findCanAccessRepositoriesByUserId(user.getId()));
	}

	@GetMapping("/repos/{owner}/{repoName}")
	public ResponseEntity<Repository> getRepo(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String repoName) {
		
		Repository project = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canRead(principal, project).orElseThrow(NoAuthorizationException::new);
		AccessLevel accessLevel = repositoryPermissionService.findTopestPermission(principal, project);
		project.setAccessLevel(accessLevel);
		return ResponseEntity.ok(project);
	}
	
	@GetMapping("/repos/{owner}/{repoName}/latest-commit/{parentId}")
	public ResponseEntity<GitCommitInfo> getLatestCommit(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String repoName,
			@PathVariable String parentId) {
		
		Integer resourceId = NumberUtil.toInt(parentId).orElseThrow(ResourceNotFoundException::new);
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canRead(principal, repository).orElseThrow(NoAuthorizationException::new);
		
		String filePath = String.join("/", repositoryResourceService.findParentPathes(resourceId));
		repository.setCreateUserName(owner);
		GitCommitInfo commitInfo = repositoryService.findLatestCommitInfo(repository, filePath).orElse(null);
		
		return ResponseEntity.ok(commitInfo);
	}
	
	@GetMapping("/repos/{owner}/{repoName}/deploy_setting")
	public ResponseEntity<DeploySetting> getDeploySetting(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String repoName) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository project = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canRead(principal, project).orElseThrow(NoAuthorizationException::new);
		
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
