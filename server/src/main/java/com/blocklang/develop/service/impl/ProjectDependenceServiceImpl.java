package com.blocklang.develop.service.impl;

import org.springframework.stereotype.Service;

import com.blocklang.core.model.UserInfo;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.marketplace.model.ComponentRepo;

@Service
public class ProjectDependenceServiceImpl implements ProjectDependenceService{

	@Override
	public Boolean buildDependenceExists(Integer projectId, Integer componentRepoId, String profileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean devDependenceExists(Integer projectId, Integer componentRepoId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProjectDependence save(Integer projectId, ComponentRepo componentRepo, UserInfo user) {
		// TODO Auto-generated method stub
		return null;
	}

}
