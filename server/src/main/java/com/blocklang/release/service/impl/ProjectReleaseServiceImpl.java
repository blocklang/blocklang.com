package com.blocklang.release.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.develop.constant.BuildTarget;
import com.blocklang.release.dao.ProjectReleaseDao;
import com.blocklang.release.model.ProjectRelease;
import com.blocklang.release.service.ProjectReleaseService;

@Service
public class ProjectReleaseServiceImpl implements ProjectReleaseService {

	@Autowired
	private ProjectReleaseDao projectReleaseDao;
	
	@Override
	public Optional<ProjectRelease> findByRepositoryIdAndProjectIdAndVersionAndBuildTarget(Integer repositoryId,
			Integer projectId, String version, BuildTarget buildTarget) {
		return projectReleaseDao.findByRepositoryIdAndProjectIdAndVersionAndBuildTarget(repositoryId, projectId, version, buildTarget);
	}

}
