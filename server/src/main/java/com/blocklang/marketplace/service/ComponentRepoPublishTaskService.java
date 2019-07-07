package com.blocklang.marketplace.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.blocklang.marketplace.data.ComponentRepoResult;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;

public interface ComponentRepoPublishTaskService {

	public ComponentRepoPublishTask save(ComponentRepoPublishTask task);

	public Optional<ComponentRepoPublishTask> findByGitUrlAndUserId(Integer userId, String gitUrl);

	/**
	 * 此方法只能用于查询某一个用户发布的组件库以及发布结果信息
	 * 
	 * @param createUserId
	 * @param query
	 * @param page
	 * @return
	 */
	Page<ComponentRepoResult> findAllByNameOrLabel(Integer createUserId, String query, Pageable page);
}
