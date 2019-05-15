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

	// read < write < admin
	protected void ensureCanRead(UserInfo user, Project project) {
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(user.getId(), project.getId());
		
		// 从 authes 中获取最大的权限
		ProjectAuthorization auth = authes.stream()
				.max((o1, o2) -> o2.getAccessLevel().getScore() - o1.getAccessLevel().getScore())
				.orElseThrow(NoAuthorizationException::new);
		
		project.setAccessLevel(auth.getAccessLevel());
		
		boolean canRead = auth.getAccessLevel().getScore() >= AccessLevel.READ.getScore();
		if(!canRead) {
			throw new NoAuthorizationException();
		}
	}

	protected void ensureCanWrite(UserInfo user, Project project) {
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(user.getId(), project.getId());
		
		// 从 authes 中获取最大的权限
		ProjectAuthorization auth = authes.stream()
				.max((o1, o2) -> o2.getAccessLevel().getScore() - o1.getAccessLevel().getScore())
				.orElseThrow(NoAuthorizationException::new);
		
		project.setAccessLevel(auth.getAccessLevel());
		
		boolean canWrite = auth.getAccessLevel().getScore() >= AccessLevel.WRITE.getScore();
		if(!canWrite) {
			throw new NoAuthorizationException();
		}
	}
}
