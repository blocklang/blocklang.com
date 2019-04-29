package com.blocklang.develop.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.develop.constant.AppType;
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

	/**
	 * 在同一层级下，根据 key 查找
	 * 
	 * @param projectId
	 * @param parentId
	 * @param resourceType
	 * @param appType
	 * @param key
	 * @return
	 */
	Optional<ProjectResource> findByKey(
			Integer projectId, 
			Integer parentId, 
			ProjectResourceType resourceType,
			AppType appType,
			String key);
	
	/**
	 * 在同一层级下，根据 name 查找
	 * 
	 * @param projectId
	 * @param parentId
	 * @param resourceType
	 * @param appType
	 * @param name
	 * @return
	 */
	Optional<ProjectResource> findByName(
			Integer projectId, 
			Integer parentId, 
			ProjectResourceType resourceType,
			AppType appType,
			String name);

	Optional<ProjectResource> findById(Integer resourceId);

}
