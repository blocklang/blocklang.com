package com.blocklang.develop.service;

import java.util.Optional;

import com.blocklang.develop.model.ProjectAuthorization;

public interface ProjectAuthorizationService {

	Optional<ProjectAuthorization> find(Integer userId, Integer projectId);

}
