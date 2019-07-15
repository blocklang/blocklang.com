package com.blocklang.marketplace.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.service.UserService;
import com.blocklang.marketplace.dao.ComponentRepoPublishTaskDao;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;
import com.blocklang.release.constant.ReleaseResult;

@Service
public class ComponentRepoPublishTaskServiceImpl implements ComponentRepoPublishTaskService{

	@Autowired
	private ComponentRepoPublishTaskDao componentRepoPublishTaskDao;
	@Autowired
	private UserService userService;
	
	@Override
	public ComponentRepoPublishTask save(ComponentRepoPublishTask task) {
		Integer maxSeq = componentRepoPublishTaskDao
			.findFirstByGitUrlAndCreateUserIdOrderBySeqDesc(task.getGitUrl(), task.getCreateUserId())
			.map(ComponentRepoPublishTask::getSeq)
			.orElse(0);
		task.setSeq(maxSeq + 1);
		return componentRepoPublishTaskDao.save(task);
	}

	@Override
	public boolean existsByCreateUserIdAndGitUrl(Integer userId, String gitUrl) {
		return componentRepoPublishTaskDao.existsByCreateUserIdAndGitUrl(userId, gitUrl);
	}

	@Override
	public List<ComponentRepoPublishTask> findUserPublishingTasks(Integer userId) {
		return componentRepoPublishTaskDao.findAllByCreateUserIdAndPublishResultOrderByCreateTimeDesc(userId, ReleaseResult.STARTED);
	}

	@Override
	public Optional<ComponentRepoPublishTask> findById(Integer taskId) {
		return componentRepoPublishTaskDao.findById(taskId).map(task ->{
			userService.findById(task.getCreateUserId()).ifPresent(user -> task.setCreateUserName(user.getLoginName()));
			return task;
		});
	}

}
