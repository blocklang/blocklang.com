package com.blocklang.develop.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectResource;

public interface ProjectResourceService {

	ProjectResource insert(Project project, ProjectResource resource);

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

	/**
	 * 只要存在项目标识，则就假定该项目肯定存在
	 * findParentIdByParentPath 中不校验项目是否存在，
	 * 在调用此方法前，就应校验过项目是否存在。
	 * 
	 * @param projectId
	 * @param parentPath
	 * @return 如果有一个分组匹配不上，则返回空数组
	 */
	List<ProjectResource> findParentGroupsByParentPath(Integer projectId, String parentPath);

}
