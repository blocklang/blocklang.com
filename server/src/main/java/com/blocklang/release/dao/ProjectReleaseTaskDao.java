package com.blocklang.release.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.ProjectReleaseTask;

public interface ProjectReleaseTaskDao extends JpaRepository<ProjectReleaseTask, Integer>{

}
