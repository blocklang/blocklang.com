package com.blocklang.release.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.blocklang.release.model.ProjectReleaseTask;

public interface ProjectReleaseTaskService {

	ProjectReleaseTask save(ProjectReleaseTask  projectReleaseTask);

	List<ProjectReleaseTask> findAllByProjectId(Integer projectId);

	Long count(Integer projectId);

	Optional<ProjectReleaseTask> findByProjectIdAndVersion(Integer projectId, String version);

	/**
	 * 获取日志文件的内容。
	 * 
	 * @param logFilePath
	 * @return
	 */
	List<String> getLogContent(Path logFilePath);

}
