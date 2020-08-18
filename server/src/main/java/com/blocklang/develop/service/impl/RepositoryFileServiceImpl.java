package com.blocklang.develop.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.Constant;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.dao.RepositoryFileDao;
import com.blocklang.develop.dao.RepositoryResourceDao;
import com.blocklang.develop.model.RepositoryFile;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.RepositoryFileService;

@Service
public class RepositoryFileServiceImpl implements RepositoryFileService {
	
	@Autowired
	private RepositoryResourceDao repositoryResourceDao;
	@Autowired
	private RepositoryFileDao repositoryFileDao;

	@Override
	public Optional<RepositoryFile> findReadme(Integer projectId) {
		return repositoryResourceDao.findByRepositoryIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
				projectId, 
				Constant.TREE_ROOT_ID,
				RepositoryResourceType.FILE,
				AppType.UNKNOWN,
				RepositoryResource.README_KEY)
			.flatMap(resource -> {
				return repositoryFileDao.findByRepositoryResourceId(resource.getId());
			});
	}

}
