package com.blocklang.develop.service;

import java.util.Optional;

import com.blocklang.develop.model.ProjectDeploy;

public interface ProjectDeployService {

	/**
	 * 获取项目的部署设置信息
	 * 
	 * 如果不存在，则创建之后返回
	 * 
	 * @param projectId 项目标识
	 * @param userId 用户标识
	 * @return 用户专属的部署设置信息
	 */
	Optional<ProjectDeploy> findOrCreate(Integer projectId, Integer userId);

	Optional<ProjectDeploy> findByRegistrationToken(String registrationToken);

}
