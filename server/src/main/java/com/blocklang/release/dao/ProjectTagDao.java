package com.blocklang.release.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.ProjectTag;

public interface ProjectTagDao extends JpaRepository<ProjectTag, Integer> {

	Optional<ProjectTag> findByProjectIdAndVersion(Integer projectId, String version);

	Optional<ProjectTag> findFirstByProjectIdOrderByIdDesc(Integer projectId);

}
