package com.blocklang.develop.service.impl;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.model.UserInfo;
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

	@Override
	public AccessLevel findTopestPermission(Principal loginUser, Project project) {
		if(loginUser == null) {
			if(project.getIsPublic()) {
				return AccessLevel.READ;
			}
			return AccessLevel.FORBIDDEN;
		}
		
		// 如果用户登录名不存在
		UserInfo user = userService.findByLoginName(loginUser.getName()).orElseThrow(NoAuthorizationException::new);
		List<ProjectAuthorization> authes = projectAuthorizationDao.findAllByUserIdAndProjectId(user.getId(), project.getId());
		if(authes.isEmpty()) {
			if(project.getIsPublic()) {
				return AccessLevel.READ;
			}
			return AccessLevel.FORBIDDEN;
		}
		
		return authes.stream().map(ProjectAuthorization::getAccessLevel)
						.max((o1, o2) -> o1.getScore() - o2.getScore()).get();
	}
}
