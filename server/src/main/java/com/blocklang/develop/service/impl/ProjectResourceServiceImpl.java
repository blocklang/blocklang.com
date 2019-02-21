package com.blocklang.develop.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.develop.dao.ProjectResourceDao;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectResourceService;

@Service
public class ProjectResourceServiceImpl implements ProjectResourceService {

	@Autowired
	private ProjectResourceDao projectResourceDao;
	
	@Transactional
	@Override
	public ProjectResource insert(ProjectResource resource) {
		return projectResourceDao.save(resource);
	}

	@Override
	public List<ProjectResource> findChildren(int projectId, int resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

}
