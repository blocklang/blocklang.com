package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.release.model.ProjectTag;

public interface ProjectTagService {

	Optional<ProjectTag> find(Integer projectId, String version);

	Optional<ProjectTag> findLatestTag(Integer projectId);

}
