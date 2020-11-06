package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.develop.constant.BuildTarget;
import com.blocklang.release.model.ProjectRelease;

public interface ProjectReleaseService {

	Optional<ProjectRelease> findByRepositoryIdAndProjectIdAndVersionAndBuildTarget(Integer repositoryId, Integer projectId, String version,
			BuildTarget buildTarget);
	
}
