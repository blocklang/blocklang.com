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
		AccessLevel accessLevel = authes.stream().map(item -> item.getAccessLevel())
				.max((o1, o2) -> o2.getScore() - o1.getScore())
				.orElse(null);
		// 对于公开项目，如果没有配置权限，则默认为 READ
		if(project.getIsPublic() && accessLevel == null) {
			project.setAccessLevel(AccessLevel.READ);
			return;
		}
		
		if(accessLevel == null) {
			throw new NoAuthorizationException();
		}
		
		project.setAccessLevel(accessLevel);
		
		boolean canRead = accessLevel.getScore() >= AccessLevel.READ.getScore();
		if(!canRead) {
			throw new NoAuthorizationException();
		}
	}

	protected void ensureCanWrite(UserInfo user, Project project) {
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(user.getId(), project.getId());
		
		// 从 authes 中获取最大的权限
		AccessLevel accessLevel = authes.stream().map(item -> item.getAccessLevel())
				.max((o1, o2) -> o2.getScore() - o1.getScore())
				.orElseThrow(NoAuthorizationException::new);
		
		project.setAccessLevel(accessLevel);
		
		boolean canWrite = accessLevel.getScore() >= AccessLevel.WRITE.getScore();
		if(!canWrite) {
			throw new NoAuthorizationException();
		}
	}
}
