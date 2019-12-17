package com.blocklang.develop.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.service.UserService;
import com.blocklang.develop.service.ProjectPermissionService;
import com.blocklang.develop.service.ProjectService;

public class AbstractProjectController {

	@Autowired 
	protected UserService userService;
	@Autowired
	protected ProjectService projectService;
	@Autowired
	protected ProjectPermissionService projectPermissionService;

}
