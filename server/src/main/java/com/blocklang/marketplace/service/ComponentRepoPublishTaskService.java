package com.blocklang.marketplace.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.marketplace.model.ComponentRepoPublishTask;

public interface ComponentRepoPublishTaskService {

	public ComponentRepoPublishTask save(ComponentRepoPublishTask task);

	public List<ComponentRepoPublishTask> findUserPublishingTasks(Integer userId);

	/**
	 * 需要获取创建者的用户名
	 * 
	 * @param taskId
	 * @return
	 */
	public Optional<ComponentRepoPublishTask> findById(Integer taskId);
}
