package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.release.model.RepositoryTag;

public interface ProjectTagService {

	Optional<RepositoryTag> find(Integer projectId, String version);

	Optional<RepositoryTag> findLatestTag(Integer projectId);

}
