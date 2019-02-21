package com.blocklang.develop.service;

import java.util.List;

import com.blocklang.develop.model.ProjectResource;

public interface ProjectResourceService {

	ProjectResource insert(ProjectResource resource);

	List<ProjectResource> findChildren(int projectId, int resourceId);

}
