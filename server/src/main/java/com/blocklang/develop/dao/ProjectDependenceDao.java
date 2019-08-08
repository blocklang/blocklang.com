package com.blocklang.develop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.ProjectDependence;

public interface ProjectDependenceDao extends JpaRepository<ProjectDependence, Integer> {

	List<ProjectDependence> findAllByProjectIdAndProfileId(Integer projectId, Integer buildProfileId);

	List<ProjectDependence> findAllByProjectId(Integer projectId);

}
