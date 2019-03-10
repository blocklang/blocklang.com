package com.blocklang.release.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.ProjectReleaseTask;

public interface ProjectReleaseTaskDao extends JpaRepository<ProjectReleaseTask, Integer>{

	List<ProjectReleaseTask> findAllByProjectId(Integer projectId, Pageable pageable);

}
