package com.blocklang.develop.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.Constant;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.dao.ProjectFileDao;
import com.blocklang.develop.dao.ProjectResourceDao;
import com.blocklang.develop.model.ProjectFile;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectFileService;

@Service
public class ProjectFileServiceImpl implements ProjectFileService {
	
	@Autowired
	private ProjectResourceDao projectResourceDao;
	@Autowired
	private ProjectFileDao projectFileDao;

	@Override
	public Optional<ProjectFile> findReadme(Integer projectId) {
		return projectResourceDao.findByProjectIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
				projectId, 
				Constant.TREE_ROOT_ID,
				ProjectResourceType.FILE,
				AppType.UNKNOWN,
				ProjectResource.README_KEY)
			.flatMap(resource -> {
				return projectFileDao.findByProjectResourceId(resource.getId());
			});
	}

}
