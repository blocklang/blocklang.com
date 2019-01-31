package com.blocklang.release.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.ProjectTag;

public interface GitTagDao extends JpaRepository<ProjectTag, Integer>{

	public Optional<ProjectTag> findByProjectIdAndVersion(Integer projectId, String version);

}
