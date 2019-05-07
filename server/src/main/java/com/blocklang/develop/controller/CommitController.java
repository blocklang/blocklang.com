package com.blocklang.develop.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.develop.constant.AccessLevel;
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
	private ProjectAuthorizationService projectAuthorizationService;
	@Autowired
	private ProjectResourceService projectResourceService;
	
	@GetMapping("/projects/{owner}/{projectName}/changes")
	public ResponseEntity<List<UncommittedFile>> listChanges(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName) {
		
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(project.getCreateUserId(), project.getId());
		boolean canWrite = authes.stream().anyMatch(item -> item.getAccessLevel() == AccessLevel.WRITE || item.getAccessLevel() == AccessLevel.ADMIN);
		if(!canWrite) {
			throw new NoAuthorizationException();
		}
		
		return ResponseEntity.ok(projectResourceService.findChanges(project));
	}
}
