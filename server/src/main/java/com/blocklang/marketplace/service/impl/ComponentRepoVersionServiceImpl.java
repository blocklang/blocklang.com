package com.blocklang.marketplace.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.blocklang.marketplace.service.ComponentRepoVersionService;

@Service
public class ComponentRepoVersionServiceImpl implements ComponentRepoVersionService{

	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	
	@Override
	public Optional<ComponentRepoVersion> findById(Integer componentRepoVersionId) {
		return componentRepoVersionDao.findById(componentRepoVersionId);
	}

}
