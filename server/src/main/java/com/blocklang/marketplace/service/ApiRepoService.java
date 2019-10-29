package com.blocklang.marketplace.service;

import java.util.Optional;

import com.blocklang.marketplace.model.ApiRepo;

public interface ApiRepoService {

	Optional<ApiRepo> findById(Integer apiRepoId);

}
