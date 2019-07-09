package com.blocklang.marketplace.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.release.constant.ReleaseResult;

public interface ComponentRepoPublishTaskDao extends JpaRepository<ComponentRepoPublishTask, Integer> {

	Optional<ComponentRepoPublishTask> findByGitUrlAndCreateUserId(String gitUrl, Integer userId);

	List<ComponentRepoPublishTask> findAllByCreateUserIdOrderByCreateTimeDesc(Integer createUserId);

	List<ComponentRepoPublishTask> findAllByCreateUserIdAndPublishResultOrderByCreateTimeDesc(Integer userId,
			ReleaseResult started);

}
