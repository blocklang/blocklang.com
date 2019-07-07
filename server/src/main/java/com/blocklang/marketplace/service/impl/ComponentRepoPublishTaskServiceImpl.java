package com.blocklang.marketplace.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ComponentRepoPublishTaskDao;
import com.blocklang.marketplace.data.ComponentRepoResult;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;

@Service
public class ComponentRepoPublishTaskServiceImpl implements ComponentRepoPublishTaskService{

	@Autowired
	private ComponentRepoPublishTaskDao componentRepoPublishTaskDao;
	
	@Override
	public ComponentRepoPublishTask save(ComponentRepoPublishTask task) {
		return componentRepoPublishTaskDao.save(task);
	}

	@Override
	public Optional<ComponentRepoPublishTask> findByGitUrlAndUserId(Integer userId, String gitUrl) {
		return componentRepoPublishTaskDao.findByGitUrlAndCreateUserId(gitUrl, userId);
	}

	@Override
	public Page<ComponentRepoResult> findAllByNameOrLabel(Integer createUserId, String query, Pageable page) {
		// TODO Auto-generated method stub
		return null;
	}

}
