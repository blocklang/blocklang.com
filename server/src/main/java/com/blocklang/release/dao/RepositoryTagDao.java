package com.blocklang.release.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.RepositoryTag;

public interface RepositoryTagDao extends JpaRepository<RepositoryTag, Integer> {

	Optional<RepositoryTag> findByRepositoryIdAndVersion(Integer repositoryId, String version);

	Optional<RepositoryTag> findFirstByRepositoryIdOrderByIdDesc(Integer repositoryId);

}
