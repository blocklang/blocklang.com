package com.blocklang.develop.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.git.exception.GitEmptyCommitException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.data.CommitMessage;
import com.blocklang.develop.data.StageParam;
import com.blocklang.develop.data.UncommittedFile;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.service.ProjectAuthorizationService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;

@RestController
public class CommitController {

	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserService userService;
	@Autowired
	private ProjectAuthorizationService projectAuthorizationService;
	@Autowired
	private ProjectResourceService projectResourceService;
	
	// 如果是公开项目，则任何人都能访问
	// 如果是私有项目，则有读的权限就能访问
	@GetMapping("/projects/{owner}/{projectName}/changes")
	public ResponseEntity<List<UncommittedFile>> listChanges(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName) {

		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		if(!project.getIsPublic()) {
			
			if(principal == null) {
				throw new NoAuthorizationException();
			}
			
			UserInfo user = userService.findByLoginName(principal.getName()).get();
			ensureCanRead(user, project);
		}
		
		return ResponseEntity.ok(projectResourceService.findChanges(project));
	}
	
	@PostMapping("/projects/{owner}/{projectName}/stage-changes")
	public ResponseEntity<Map<String, Object>> stageChanges(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@RequestBody StageParam param) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		ensureCanWrite(user, project);
		
		projectResourceService.stageChanges(project, param.getFilePathes());
		
		return ResponseEntity.ok(new HashMap<String, Object>());
	}
	
	@PostMapping("/projects/{owner}/{projectName}/unstage-changes")
	public ResponseEntity<Map<String, Object>> unstageChanges(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@RequestBody StageParam param) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		ensureCanWrite(user, project);
		
		projectResourceService.unstageChanges(project, param.getFilePathes());
		
		return ResponseEntity.ok(new HashMap<String, Object>());
	}
	
	@PostMapping("/projects/{owner}/{projectName}/commits")
	public ResponseEntity<Map<String, Object>> commit(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@RequestBody CommitMessage param,
			BindingResult bindingResult) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		ensureCanWrite(user, project);
		
		try {
			projectResourceService.commit(user, project, param.getValue());
		} catch(GitEmptyCommitException e) {
			bindingResult.reject("NotEmpty.gitCommit");
		}
		
		if(bindingResult.hasErrors()) {
			throw new InvalidRequestException(bindingResult);
		}
		
		return ResponseEntity.ok(new HashMap<String, Object>());
	}

	private void ensureCanRead(UserInfo user, Project project) {
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(user.getId(), project.getId());
		boolean canRead = authes.stream().anyMatch(
				item -> item.getAccessLevel() == AccessLevel.WRITE || 
				item.getAccessLevel() == AccessLevel.ADMIN ||
				item.getAccessLevel() == AccessLevel.READ);
		if(!canRead) {
			throw new NoAuthorizationException();
		}
	}
	
	private void ensureCanWrite(UserInfo user, Project project) {
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(user.getId(), project.getId());
		boolean canWrite = authes.stream().anyMatch(
				item -> item.getAccessLevel() == AccessLevel.WRITE || 
				item.getAccessLevel() == AccessLevel.ADMIN);
		if(!canWrite) {
			throw new NoAuthorizationException();
		}
	}
}
