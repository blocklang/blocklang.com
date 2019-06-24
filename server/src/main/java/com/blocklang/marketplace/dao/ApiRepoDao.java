package com.blocklang.marketplace.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiRepo;

public interface ApiRepoDao extends JpaRepository<ApiRepo, Integer> {

	Optional<ApiRepo> findByNameAndCreateUserId(String name, Integer createUserId);

	Optional<ApiRepo> findByGitRepoUrlAndCreateUserId(String gitRepoUrl, Integer createUserId);

}
