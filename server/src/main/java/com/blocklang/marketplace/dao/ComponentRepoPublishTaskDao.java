package com.blocklang.marketplace.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.release.constant.ReleaseResult;

public interface ComponentRepoPublishTaskDao extends JpaRepository<ComponentRepoPublishTask, Integer> {

	/**
	 * 
	 * @param gitUrl
	 * @param userId
	 * @return
	 * @deprecated 一个用户对同一个组件库，可以发布多次，如第一次发布和后续升级，所以此接口应返回 List
	 */
	Optional<ComponentRepoPublishTask> findByGitUrlAndCreateUserId(String gitUrl, Integer userId);

	List<ComponentRepoPublishTask> findAllByCreateUserIdOrderByCreateTimeDesc(Integer createUserId);

	List<ComponentRepoPublishTask> findAllByCreateUserIdAndPublishResultOrderByCreateTimeDesc(Integer userId,
			ReleaseResult started);
	
	Optional<ComponentRepoPublishTask> findFirstByGitUrlAndCreateUserIdOrderBySeqDesc(String gitUrl, Integer userId);

}
