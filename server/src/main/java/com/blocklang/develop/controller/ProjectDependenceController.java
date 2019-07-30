package com.blocklang.develop.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectResourceService;

/**
 * 项目依赖
 * 
 * @author Zhengwei Jin
 *
 */
@RestController
public class ProjectDependenceController extends AbstractProjectController{

	@Autowired
	private UserService userService;
	@Autowired
	private ProjectResourceService projectResourceService;

	@GetMapping("/projects/{owner}/{projectName}/dependence")
	public ResponseEntity<Map<String, Object>> getDependence(
			Principal principal,
			@PathVariable String owner,
			@PathVariable String projectName) {
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		if(project.getIsPublic()) {
			if(principal == null) {
				project.setAccessLevel(AccessLevel.READ);
			} else {
				UserInfo user = userService.findByLoginName(principal.getName()).get();
				ensureCanRead(user, project);
			}
		} else {
			if(principal == null) {
				throw new NoAuthorizationException();
			}
			
			UserInfo user = userService.findByLoginName(principal.getName()).get();
			ensureCanRead(user, project);
		}
		
		ProjectResource resource = projectResourceService.findByKey(
				project.getId(), 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.DEPENDENCE, 
				AppType.UNKNOWN, 
				ProjectResource.DEPENDENCE_KEY).orElseThrow(ResourceNotFoundException::new);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("resourceId", resource.getId());
		
		result.put("pathes", stripResourcePathes(Collections.singletonList(resource)));
		result.put("dependences", null);
		return ResponseEntity.ok(result);
	}
	
}
