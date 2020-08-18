package com.blocklang.release.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.release.dao.ProjectTagDao;
import com.blocklang.release.model.RepositoryTag;
import com.blocklang.release.service.ProjectTagService;

@Service
public class ProjectTagServiceImpl implements ProjectTagService {

	@Autowired
	private ProjectTagDao projectTagDao;
	
	@Override
	public Optional<RepositoryTag> find(Integer projectId, String version) {
		return projectTagDao.findByProjectIdAndVersion(projectId, version);
	}

	@Override
	public Optional<RepositoryTag> findLatestTag(Integer projectId) {
		return projectTagDao.findFirstByProjectIdOrderByIdDesc(projectId);
	}

}
