package com.blocklang.marketplace.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.marketplace.model.GitRepoPublishTask;

public interface GitRepoPublishTaskService {

	public GitRepoPublishTask save(GitRepoPublishTask task);

	public List<GitRepoPublishTask> findUserPublishingTasks(Integer userId);

	/**
	 * 需要获取创建者的用户名
	 * 
	 * @param taskId
	 * @return
	 */
	public Optional<GitRepoPublishTask> findById(Integer taskId);
}
