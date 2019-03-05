package com.blocklang.develop.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.ProjectDeploy;

public interface ProjectDeployDao extends JpaRepository<ProjectDeploy, Integer> {

	Optional<ProjectDeploy> findByRegistrationToken(String registrationToken);

	Optional<ProjectDeploy> findByProjectIdAndUserId(Integer projectId, Integer userId);

}
