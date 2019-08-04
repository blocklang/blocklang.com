package com.blocklang.develop.service;

import com.blocklang.core.model.UserInfo;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.marketplace.model.ComponentRepo;

public interface ProjectDependenceService {

	Boolean buildDependenceExists(Integer projectId, Integer componentRepoId, String profileName);

	/**
	 *  dev 仓库不存在 profile 一说
	 *  
	 * @param projectId
	 * @param componentRepoId
	 * @return
	 */
	Boolean devDependenceExists(Integer projectId, Integer componentRepoId);

	ProjectDependence save(Integer projectId, ComponentRepo componentRepo, UserInfo user);

}
