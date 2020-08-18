package com.blocklang.develop.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.RepositoryFile;

public interface RepositoryFileDao extends JpaRepository<RepositoryFile, Integer> {

	Optional<RepositoryFile> findByRepositoryResourceId(Integer resourceId);

}
