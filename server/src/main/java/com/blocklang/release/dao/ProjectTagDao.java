package com.blocklang.release.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.RepositoryTag;

public interface ProjectTagDao extends JpaRepository<RepositoryTag, Integer> {

	Optional<RepositoryTag> findByProjectIdAndVersion(Integer projectId, String version);

	Optional<RepositoryTag> findFirstByProjectIdOrderByIdDesc(Integer projectId);

}
