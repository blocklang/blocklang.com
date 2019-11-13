package com.blocklang.develop.service.impl;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.service.UserService;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.dao.ProjectAuthorizationDao;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.service.ProjectPermissionService;

//read < write < admin
@Service
public class ProjectPermissionServiceImpl implements ProjectPermissionService {

	@Autowired
	private ProjectAuthorizationDao projectAuthorizationDao;
	@Autowired
	private UserService userService;
	
	@Override
	public Optional<AccessLevel> canRead(Principal loginUser, Project project) {
		if (project.getIsPublic()) {
			return Optional.of(AccessLevel.READ);
		}

		if (loginUser == null) {
			return Optional.empty();
		}
		
		return hasPermission(loginUser.getName(), project, AccessLevel.READ);
	}
	
	@Override
	public Optional<AccessLevel> canWrite(Principal loginUser, Project project) {
		if (loginUser == null) {
			return Optional.empty();
		}
		
		return hasPermission(loginUser.getName(), project, AccessLevel.WRITE);
	}

	@Override
	public Optional<AccessLevel> canAdmin(Principal loginUser, Project project) {
		if (loginUser == null) {
			return Optional.empty();
		}
		
		return hasPermission(loginUser.getName(), project, AccessLevel.ADMIN);
	}
	
	private Optional<AccessLevel> hasPermission(String loginName, Project project, AccessLevel expectedPermission) {
		return userService.findByLoginName(loginName)
				.flatMap(user -> projectAuthorizationDao.findAllByUserIdAndProjectId(user.getId(), project.getId())
						.stream().map(ProjectAuthorization::getAccessLevel)
						.filter(accessLevel -> accessLevel.getScore() >= expectedPermission.getScore()).findAny());
	}
}
