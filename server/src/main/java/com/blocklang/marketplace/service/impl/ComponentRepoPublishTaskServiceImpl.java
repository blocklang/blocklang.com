package com.blocklang.marketplace.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoPublishTaskDao;
import com.blocklang.marketplace.data.ComponentRepoResult;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;

@Service
public class ComponentRepoPublishTaskServiceImpl implements ComponentRepoPublishTaskService{

	@Autowired
	private ComponentRepoPublishTaskDao componentRepoPublishTaskDao;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	
	@Override
	public ComponentRepoPublishTask save(ComponentRepoPublishTask task) {
		return componentRepoPublishTaskDao.save(task);
	}

	@Override
	public Optional<ComponentRepoPublishTask> findByGitUrlAndUserId(Integer userId, String gitUrl) {
		return componentRepoPublishTaskDao.findByGitUrlAndCreateUserId(gitUrl, userId);
	}

	@Override
	public List<ComponentRepoResult> findComponentRepos(Integer createUserId) {
		List<ComponentRepoPublishTask> tasks = componentRepoPublishTaskDao.findAllByCreateUserIdOrderByCreateTimeDesc(createUserId);
		
		return tasks.stream().map(task -> {
			ComponentRepoResult result = new ComponentRepoResult();
			result.setPublishTask(task);
			
			componentRepoDao.findByGitRepoUrlAndCreateUserId(task.getGitUrl(), createUserId).ifPresent(repo -> result.setComponentRepo(repo));
			
			return result;
		}).collect(Collectors.toList());
	}

}
