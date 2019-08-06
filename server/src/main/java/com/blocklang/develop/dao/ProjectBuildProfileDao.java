package com.blocklang.develop.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.model.ProjectBuildProfile;

public interface ProjectBuildProfileDao extends JpaRepository<ProjectBuildProfile, Integer> {

	Optional<ProjectBuildProfile> findByProjectIdAndAppTypeAndNameIgnoreCase(Integer projectId, AppType appType, String profileName);

}
