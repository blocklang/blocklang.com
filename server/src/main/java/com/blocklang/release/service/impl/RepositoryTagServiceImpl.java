package com.blocklang.release.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.release.dao.RepositoryTagDao;
import com.blocklang.release.model.RepositoryTag;
import com.blocklang.release.service.RepositoryTagService;

@Service
public class RepositoryTagServiceImpl implements RepositoryTagService {

	@Autowired
	private RepositoryTagDao repositoryTagDao;
	
	@Override
	public Optional<RepositoryTag> find(Integer repositoryId, String version) {
		return repositoryTagDao.findByRepositoryIdAndVersion(repositoryId, version);
	}

	@Override
	public Optional<RepositoryTag> findLatestTag(Integer repositoryId) {
		return repositoryTagDao.findFirstByRepositoryIdOrderByIdDesc(repositoryId);
	}

}
