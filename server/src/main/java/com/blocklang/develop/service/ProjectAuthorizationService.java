package com.blocklang.develop.service;

import java.util.List;

import com.blocklang.develop.model.ProjectAuthorization;

public interface ProjectAuthorizationService {

	List<ProjectAuthorization> findAllByUserIdAndProjectId(Integer userId, Integer projectId);

}
