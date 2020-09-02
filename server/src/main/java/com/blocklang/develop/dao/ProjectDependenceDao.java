package com.blocklang.develop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.ProjectDependency;

public interface ProjectDependenceDao extends JpaRepository<ProjectDependency, Integer> {

	List<ProjectDependency> findAllByProjectIdAndProfileId(Integer projectId, Integer buildProfileId);

	List<ProjectDependency> findAllByProjectId(Integer projectId);

}
