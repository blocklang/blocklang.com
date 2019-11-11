package com.blocklang.marketplace.service;

import java.util.Optional;

import com.blocklang.marketplace.model.ApiRepoVersion;

public interface ApiRepoVersionService {

	Optional<ApiRepoVersion> findById(Integer apiRepoVersionId);

	Optional<ApiRepoVersion> findLatestVersion(Integer apiRepoId);
}
