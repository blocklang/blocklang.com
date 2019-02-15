package com.blocklang.develop.controller;

import java.security.Principal;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.develop.data.CheckProjectNameParam;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.release.controller.ReleaseController;

@RestController
public class ProjectController {

	private static final Logger logger = LoggerFactory.getLogger(ReleaseController.class);
	
	@Autowired
	private ProjectService projectService;
	
	@PostMapping("/projects/check-name")
	public ResponseEntity<Void> checkProjectName(
			Principal principal,
			@Valid @RequestBody CheckProjectNameParam param, 
			BindingResult bindingResult) {
		if(principal == null || !principal.getName().equals(param.getOwner())) {
			throw new NoAuthorizationException();
		}
		
		if(bindingResult.hasErrors()) {
			logger.error("项目名称校验未通过。");
			throw new InvalidRequestException(bindingResult);
		}
		
		projectService.find(param.getOwner(), param.getValue()).ifPresent((project) -> {
			logger.error("项目名 {} 已被占用", param.getValue());
			bindingResult.rejectValue("value", "Duplicated.projectName", new Object[] {
				param.getOwner(), param.getValue()
			}, null);
			throw new InvalidRequestException(bindingResult);
		});
		
		return ResponseEntity.ok().build();
	}
}
