package com.blocklang.develop.service;

import java.util.List;

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
	List<ProjectResource> findChildren(Project project, int parentResourceId);
	
	String findParentPath(Integer resourceId);

}
