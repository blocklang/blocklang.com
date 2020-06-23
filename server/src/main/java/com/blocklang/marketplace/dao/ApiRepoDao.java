package com.blocklang.marketplace.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiRepo;

public interface ApiRepoDao extends JpaRepository<ApiRepo, Integer> {

	Optional<ApiRepo> findByGitRepoUrlAndCreateUserId(String gitUrl, Integer createUserId);

}
