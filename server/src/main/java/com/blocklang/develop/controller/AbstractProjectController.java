package com.blocklang.develop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.service.ProjectAuthorizationService;
import com.blocklang.develop.service.ProjectService;

public class AbstractProjectController {

	@Autowired
	protected ProjectService projectService;
	@Autowired
	protected ProjectAuthorizationService projectAuthorizationService;

	protected void ensureCanRead(UserInfo user, Project project) {
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(user.getId(), project.getId());
		boolean canRead = authes.stream().anyMatch(
				item -> item.getAccessLevel() == AccessLevel.WRITE || 
				item.getAccessLevel() == AccessLevel.ADMIN ||
				item.getAccessLevel() == AccessLevel.READ);
		if(!canRead) {
			throw new NoAuthorizationException();
		}
	}

	protected void ensureCanWrite(UserInfo user, Project project) {
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(user.getId(), project.getId());
		boolean canWrite = authes.stream().anyMatch(
				item -> item.getAccessLevel() == AccessLevel.WRITE || 
				item.getAccessLevel() == AccessLevel.ADMIN);
		if(!canWrite) {
			throw new NoAuthorizationException();
		}
	}

}
