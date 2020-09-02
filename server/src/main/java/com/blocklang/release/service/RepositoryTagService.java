package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.release.model.RepositoryTag;

public interface RepositoryTagService {

	Optional<RepositoryTag> find(Integer repositoryId, String version);

	Optional<RepositoryTag> findLatestTag(Integer repositoryId);

}
