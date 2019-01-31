package com.blocklang.release.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.ProjectBuild;

public interface ProjectBuildDao extends JpaRepository<ProjectBuild, Integer> {

	Optional<ProjectBuild> findByProjectTagId(Integer projectTagId);

}
