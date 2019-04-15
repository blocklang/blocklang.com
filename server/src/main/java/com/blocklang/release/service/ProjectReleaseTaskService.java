package com.blocklang.release.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.release.model.ProjectReleaseTask;

public interface ProjectReleaseTaskService {

	ProjectReleaseTask save(ProjectReleaseTask  projectReleaseTask);

	List<ProjectReleaseTask> findAllByProjectId(Integer projectId);

	Long count(Integer projectId);

	Optional<ProjectReleaseTask> findByProjectIdAndVersion(Integer projectId, String version);

	//List<ProjectReleaseTask> findAllByReleaseResult(ReleaseResult result);

}
