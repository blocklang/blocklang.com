package com.blocklang.develop.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.ProjectFile;

public interface ProjectFileDao extends JpaRepository<ProjectFile, Integer> {

	Optional<ProjectFile> findByProjectResourceId(Integer resourceId);

}
