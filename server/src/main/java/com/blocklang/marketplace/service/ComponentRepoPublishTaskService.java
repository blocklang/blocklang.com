package com.blocklang.marketplace.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.marketplace.model.ComponentRepoPublishTask;

public interface ComponentRepoPublishTaskService {

	public ComponentRepoPublishTask save(ComponentRepoPublishTask task);

	public Optional<ComponentRepoPublishTask> findByGitUrlAndUserId(Integer userId, String gitUrl);

	public List<ComponentRepoPublishTask> findUserPublishingTasks(Integer userId);
}
