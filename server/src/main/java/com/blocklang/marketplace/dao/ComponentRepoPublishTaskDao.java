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
	 */
	boolean existsByCreateUserIdAndGitUrl(Integer userId, String gitUrl);

	List<ComponentRepoPublishTask> findAllByCreateUserIdOrderByCreateTimeDesc(Integer createUserId);

	List<ComponentRepoPublishTask> findAllByCreateUserIdAndPublishResultOrderByCreateTimeDesc(Integer userId,
			ReleaseResult started);
	
	Optional<ComponentRepoPublishTask> findFirstByGitUrlAndCreateUserIdOrderBySeqDesc(String gitUrl, Integer userId);

}
