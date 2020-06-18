package com.blocklang.marketplace.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.GitRepoPublishTask;
import com.blocklang.release.constant.ReleaseResult;

public interface GitRepoPublishTaskDao extends JpaRepository<GitRepoPublishTask, Integer> {

	List<GitRepoPublishTask> findAllByCreateUserIdOrderByCreateTimeDesc(Integer createUserId);

	List<GitRepoPublishTask> findAllByCreateUserIdAndPublishResultOrderByCreateTimeDesc(Integer userId,
			ReleaseResult started);
	
	Optional<GitRepoPublishTask> findFirstByGitUrlAndCreateUserIdOrderBySeqDesc(String gitUrl, Integer userId);

}
