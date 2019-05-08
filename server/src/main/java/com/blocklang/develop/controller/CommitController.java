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
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
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
			
			List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(user.getId(), project.getId());
			boolean canRead = authes.stream().anyMatch(
					item -> item.getAccessLevel() == AccessLevel.WRITE || 
					item.getAccessLevel() == AccessLevel.ADMIN ||
					item.getAccessLevel() == AccessLevel.READ);
			if(!canRead) {
				throw new NoAuthorizationException();
			}
		}
		
		return ResponseEntity.ok(projectResourceService.findChanges(project));
	}
}
