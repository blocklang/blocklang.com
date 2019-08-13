package com.blocklang.marketplace.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.marketplace.model.ComponentRepoVersion;

public interface ComponentRepoVersionService {

	Optional<ComponentRepoVersion> findById(Integer componentRepoVersionId);

	List<ComponentRepoVersion> findByComponentRepoId(Integer componentRepoId);

}
