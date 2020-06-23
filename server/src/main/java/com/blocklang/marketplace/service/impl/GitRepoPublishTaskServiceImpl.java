package com.blocklang.marketplace.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.service.UserService;
import com.blocklang.marketplace.dao.GitRepoPublishTaskDao;
import com.blocklang.marketplace.model.GitRepoPublishTask;
import com.blocklang.marketplace.service.GitRepoPublishTaskService;
import com.blocklang.release.constant.ReleaseResult;

@Service
public class GitRepoPublishTaskServiceImpl implements GitRepoPublishTaskService{

	@Autowired
	private GitRepoPublishTaskDao gitRepoPublishTaskDao;
	@Autowired
	private UserService userService;
	
	@Override
	public GitRepoPublishTask save(GitRepoPublishTask task) {
		Integer maxSeq = gitRepoPublishTaskDao
			.findFirstByGitUrlAndCreateUserIdOrderBySeqDesc(task.getGitUrl(), task.getCreateUserId())
			.map(GitRepoPublishTask::getSeq)
			.orElse(0);
		task.setSeq(maxSeq + 1);
		return gitRepoPublishTaskDao.save(task);
	}

	@Override
	public List<GitRepoPublishTask> findUserPublishingTasks(Integer userId) {
		return gitRepoPublishTaskDao.findAllByCreateUserIdAndPublishResultOrderByCreateTimeDesc(userId, ReleaseResult.STARTED);
	}

	@Override
	public Optional<GitRepoPublishTask> findById(Integer taskId) {
		return gitRepoPublishTaskDao.findById(taskId).map(task ->{
			userService.findById(task.getCreateUserId()).ifPresent(user -> task.setCreateUserName(user.getLoginName()));
			return task;
		});
	}

}
