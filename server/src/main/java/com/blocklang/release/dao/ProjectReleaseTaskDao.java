package com.blocklang.release.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.ProjectReleaseTask;

public interface ProjectReleaseTaskDao extends JpaRepository<ProjectReleaseTask, Integer>{

	List<ProjectReleaseTask> findAllByProjectId(Integer projectId, Pageable pageable);

	Long countByProjectId(Integer projectId);

	/**
	 * @deprecated 一个项目的同一个版本会发布多次，返回 Optional 不合适
	 * 
	 * @param projectId
	 * @param version
	 * @return
	 */
	Optional<ProjectReleaseTask> findByProjectIdAndVersion(Integer projectId, String version);

	Optional<ProjectReleaseTask> findFirstByProjectIdAndVersionOrderByIdDesc(Integer projectId, String version);

}
