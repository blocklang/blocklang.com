package com.blocklang.marketplace.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.blocklang.marketplace.model.ComponentRepo;

public interface ComponentRepoDao extends JpaRepository<ComponentRepo, Integer>, JpaSpecificationExecutor<ComponentRepo>{
	
	Page<ComponentRepo> findAllByGitRepoNameContainingIgnoreCase(String queryGitRepoName, Pageable page);
	
	Optional<ComponentRepo> findByGitRepoUrlAndCreateUserId(String gitUrl, Integer userId);

	List<ComponentRepo> findAllByCreateUserIdOrderByGitRepoName(Integer userId);

	boolean existsByCreateUserIdAndGitRepoUrl(Integer userId, String gitRepoUrl);

}
