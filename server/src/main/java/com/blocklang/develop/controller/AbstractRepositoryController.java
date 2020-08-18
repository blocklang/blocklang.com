package com.blocklang.develop.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.service.UserService;
import com.blocklang.develop.service.RepositoryPermissionService;
import com.blocklang.develop.service.RepositoryService;

public class AbstractRepositoryController {

	@Autowired 
	protected UserService userService;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected RepositoryPermissionService repositoryPermissionService;

}
