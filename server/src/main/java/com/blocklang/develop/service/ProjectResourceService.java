package com.blocklang.develop.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectResource;

public interface ProjectResourceService {

	ProjectResource insert(ProjectResource resource);

	/**
	 * 获取项目结构，其中包含模块的提交信息
	 * 
	 * @param project
	 * @param parentResourceId
	 * @return 项目结构
	 */
	List<ProjectResource> findChildren(Project project, Integer parentResourceId);
	
	String findParentPath(Integer resourceId);

	Optional<ProjectResource> find(
			Integer projectId, 
			Integer parentId, 
			ProjectResourceType resourceType, 
			String key);

	Optional<ProjectResource> findById(Integer resourceId);

}
