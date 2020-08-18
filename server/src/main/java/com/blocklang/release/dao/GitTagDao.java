package com.blocklang.release.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.RepositoryTag;

public interface GitTagDao extends JpaRepository<RepositoryTag, Integer>{

	public Optional<RepositoryTag> findByProjectIdAndVersion(Integer projectId, String version);

}
