package com.blocklang.marketplace.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiRepoVersion;

public interface ApiRepoVersionDao extends JpaRepository<ApiRepoVersion, Integer> {

	Optional<ApiRepoVersion> findByApiRepoIdAndVersion(Integer apiRepoId, String version);

}
