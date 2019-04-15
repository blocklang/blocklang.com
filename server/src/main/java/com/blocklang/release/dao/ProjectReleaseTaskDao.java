package com.blocklang.release.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.ProjectReleaseTask;

public interface ProjectReleaseTaskDao extends JpaRepository<ProjectReleaseTask, Integer>{

	List<ProjectReleaseTask> findAllByProjectId(Integer projectId, Pageable pageable);

	Long countByProjectId(Integer projectId);

	Optional<ProjectReleaseTask> findByProjectIdAndVersion(Integer projectId, String version);

}
