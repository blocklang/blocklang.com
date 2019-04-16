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
	 * 获取日志文件的部分内容，只读到指定的行号。
	 * 
	 * @param logFilePath
	 * @param endLine 读到指定的行号，但不包含改行的内容，如果 endLine 的值为 null，则读取文件所有内容
	 * @return
	 */
	List<String> getLogContent(Path logFilePath, Integer endLine);

}
