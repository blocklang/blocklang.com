package com.blocklang.release.service;

import java.util.List;

import com.blocklang.release.model.ProjectReleaseTask;

public interface ProjectReleaseTaskService {

	ProjectReleaseTask save(ProjectReleaseTask  projectReleaseTask);

	List<ProjectReleaseTask> findAllByProjectId(Integer projectId);

	Long count(Integer projectId);

}
