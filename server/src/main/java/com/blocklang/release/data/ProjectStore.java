package com.blocklang.release.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;


/**
 * 指定项目模型和项目源码的存储位置
 * 
 * 存储项目模型的结构，是一个 git 仓库
 * <pre>
 * gitRepo
 *     {owner}
 *         {repository_name}
 *             .git
 *             {project_name}
 * </pre>
 * 
 * 存储项目源码的结构
 * <pre>
 * projects
 *     {owner}
 *         {repository_name}
 *             {project_name}
 *                 source
 *                     {appType}
 *                         {profile}
 *                             client
 *                                 .git
 *                             server
 *                                 .git
 *                 buildLogs
 *                     {version}-{yyyy_MM_dd_HH_mm_ss}-{git short commit id}.log
 * </pre>
 * 
 * 
 * @author jinzw
 *
 */
public class ProjectStore {

	private String dataRootPath;
	private String owner;
	private String repositoryName;
	private String projectName;
	private String version;
	
	private Path logFilePath;
	
	public ProjectStore(String dataRootPath, 
			String owner, 
			String repositoryName, 
			String projectName, 
			String version) {
		this.dataRootPath = dataRootPath;
		this.owner = owner;
		this.repositoryName = repositoryName;
		this.projectName = projectName;
		this.version = version;
	}

	/**
	 * 获取日志路径，此方法只获取完整路径，不创建目录和文件
	 * 
	 * @param gitShortCommitId
	 * @return
	 */
	public Path getLogFilePath(String gitShortCommitId){
		if(this.logFilePath == null) {
			String logFileName = generateLogFileName(gitShortCommitId);
			this.logFilePath = Paths.get(this.dataRootPath)
					.resolve("projects")
					.resolve(this.owner)
					.resolve(this.repositoryName)
					.resolve(this.projectName)
					.resolve("buildLogs")
					.resolve(logFileName);
		}
		return this.logFilePath;
	}

	private String generateLogFileName(String gitShortCommitId) {
		if(StringUtils.isBlank(gitShortCommitId)) {
			gitShortCommitId = StringUtils.repeat('0', 7);
		}
		return this.version + 
				"-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + 
				"-" + gitShortCommitId + ".log";
	}

}
