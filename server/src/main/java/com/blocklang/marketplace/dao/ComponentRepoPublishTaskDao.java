package com.blocklang.marketplace.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ComponentRepoPublishTask;

public interface ComponentRepoPublishTaskDao extends JpaRepository<ComponentRepoPublishTask, Integer> {

	Optional<ComponentRepoPublishTask> findByGitUrlAndCreateUserId(String gitUrl, Integer userId);

}
