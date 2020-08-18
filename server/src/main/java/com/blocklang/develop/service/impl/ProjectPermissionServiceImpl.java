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
import com.blocklang.develop.dao.RepositoryAuthorizationDao;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryAuthorization;
import com.blocklang.develop.service.RepositoryPermissionService;

//read < write < admin
@Service
public class ProjectPermissionServiceImpl implements RepositoryPermissionService {

	@Autowired
	private RepositoryAuthorizationDao projectAuthorizationDao;
	@Autowired
	private UserService userService;
	
	@Override
	public Optional<AccessLevel> canRead(Principal loginUser, Repository project) {
		if (project.getIsPublic()) {
			return Optional.of(AccessLevel.READ);
		}

		if (loginUser == null) {
			return Optional.empty();
		}
		
		return hasPermission(loginUser.getName(), project, AccessLevel.READ);
	}
	
	@Override
	public Optional<AccessLevel> canWrite(Principal loginUser, Repository project) {
		if (loginUser == null) {
			return Optional.empty();
		}
		
		return hasPermission(loginUser.getName(), project, AccessLevel.WRITE);
	}

	@Override
	public Optional<AccessLevel> canAdmin(Principal loginUser, Repository project) {
		if (loginUser == null) {
			return Optional.empty();
		}
		
		return hasPermission(loginUser.getName(), project, AccessLevel.ADMIN);
	}
	
	private Optional<AccessLevel> hasPermission(String loginName, Repository project, AccessLevel expectedPermission) {
		return userService.findByLoginName(loginName)
				.flatMap(user -> projectAuthorizationDao.findAllByUserIdAndRepositoryId(user.getId(), project.getId())
						.stream().map(RepositoryAuthorization::getAccessLevel)
						.filter(accessLevel -> accessLevel.getScore() >= expectedPermission.getScore()).findAny());
	}

	@Override
	public AccessLevel findTopestPermission(Principal loginUser, Repository project) {
		if(loginUser == null) {
			if(project.getIsPublic()) {
				return AccessLevel.READ;
			}
			return AccessLevel.FORBIDDEN;
		}
		
		// 如果用户登录名不存在
		UserInfo user = userService.findByLoginName(loginUser.getName()).orElseThrow(NoAuthorizationException::new);
		List<RepositoryAuthorization> authes = projectAuthorizationDao.findAllByUserIdAndRepositoryId(user.getId(), project.getId());
		if(authes.isEmpty()) {
			if(project.getIsPublic()) {
				return AccessLevel.READ;
			}
			return AccessLevel.FORBIDDEN;
		}
		
		return authes.stream().map(RepositoryAuthorization::getAccessLevel)
						.max((o1, o2) -> o1.getScore() - o2.getScore()).get();
	}
}
