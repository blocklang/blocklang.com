package com.blocklang.marketplace.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ComponentRepoPublishTaskDao;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;
import com.blocklang.release.constant.ReleaseResult;

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
	public List<ComponentRepoPublishTask> findUserPublishingTasks(Integer userId) {
		return componentRepoPublishTaskDao.findAllByCreateUserIdAndPublishResultOrderByCreateTimeDesc(userId, ReleaseResult.STARTED);
	}

}
