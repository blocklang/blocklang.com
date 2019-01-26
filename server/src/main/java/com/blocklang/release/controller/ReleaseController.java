package com.blocklang.release.controller;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.develop.service.ProjectService;
import com.blocklang.release.data.NewReleaseParam;
import com.blocklang.release.exception.InvalidRequestException;
import com.blocklang.release.exception.ResourceNotFoundException;

@RestController
public class ReleaseController {

	private static final Logger logger = LoggerFactory.getLogger(ReleaseController.class);
	
	@Autowired
	private ProjectService projectService;
	
	@PostMapping("projects/{owner}/{projectName}/releases")
	public ResponseEntity<?> newRelease(
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@Valid @RequestBody NewReleaseParam release,
			BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			logger.error("发布信息不完整：{}", release);
			throw new InvalidRequestException(bindingResult);
		}
		
		projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		
		return null;
		
	}
}
