package com.blocklang.develop.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.model.ProjectResource;
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
		ensureCanWrite(user.getId(), project);
	}
	
	protected void ensureCanWrite(Integer userId, Project project) {
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(userId, project.getId());
		
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

	/**
	 * 对一条路径中的资源进行处理，并返回处理后的数据
	 * 
	 * @param resources 资源列表，按照资源路径的顺序存储，如 a/b/c/d
	 * @return
	 */
	protected List<Map<String, String>> stripResourcePathes(List<ProjectResource> resources) {
		List<Map<String, String>> stripedParentGroups = new ArrayList<Map<String, String>>();
		String relativePath = "";
		for(ProjectResource each : resources) {
			relativePath = relativePath + "/" + each.getKey();
			
			Map<String, String> map = new HashMap<String, String>();
			if(StringUtils.isBlank(each.getName())) {
				map.put("name", each.getKey());
			} else {
				map.put("name", each.getName());
			}
			
			map.put("path", relativePath);
			stripedParentGroups.add(map);
		}
		return stripedParentGroups;
	}
}
