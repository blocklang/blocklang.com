package com.blocklang.release.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.constant.BuildTarget;
import com.blocklang.release.model.ProjectRelease;

public interface ProjectReleaseDao extends JpaRepository<ProjectRelease, Integer>{

	Optional<ProjectRelease> findByRepositoryIdAndProjectIdAndVersionAndBuildTarget(Integer repositoryId, Integer projectId, String version,
			BuildTarget buildTarget);

}
