package com.blocklang.develop.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.service.UserService;
import com.blocklang.develop.data.CheckProjectNameParam;
import com.blocklang.develop.data.NewProjectParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.service.ProjectService;

@RestController
public class ProjectController {

	private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserService userService;
	
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
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		if(OAuth2AuthenticationToken.class.isInstance(principal)) {
			OAuth2AuthenticationToken token = (OAuth2AuthenticationToken)principal;
			Map<String, Object> userAttributes = token.getPrincipal().getAttributes();
			// 因为客户端并不需要显示登录用户的登录标识，所以不返回 userId
			
			String loginName = userAttributes.get("loginName").toString();
			if(loginName.equals(owner)) {
				return;
			}
		}

		throw new NoAuthorizationException();
		
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
}
