package com.blocklang.develop.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.develop.dao.ProjectAuthorizationDao;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.service.ProjectAuthorizationService;

@Service
public class ProjectAuthorizationServiceImpl implements ProjectAuthorizationService {

	@Autowired
	private ProjectAuthorizationDao projectAuthorizationDao;
	
	@Override
	public List<ProjectAuthorization> findAllByUserIdAndProjectId(Integer userId, Integer projectId) {
		return projectAuthorizationDao.findAllByUserIdAndProjectId(userId, projectId);
	}

}
