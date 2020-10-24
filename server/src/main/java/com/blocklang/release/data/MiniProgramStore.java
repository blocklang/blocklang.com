package com.blocklang.release.data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

import com.blocklang.develop.constant.BuildTarget;


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
 *                     {buildTarget}
 *                         {profile}
 *                             RELEASE.json    // 存储最新的构建位置，是项目目录的 commit id
 *                             .git
 *                 buildLogs
 *                     {buildTarget}
 *                         {profile}
 *                             {version}-{yyyy_MM_dd_HH_mm_ss}-{git short commit id}.log
 * </pre>
 * 
 * release.json 的格式为
 * 
 * <pre>
 * {
 *     client: "项目目录的 commit id，构建成功后存储最新的 commit id",
 *     server: ""
 * }
 * </pre>
 * 
 * @author jinzw
 *
 */
public class MiniProgramStore extends ProjectStore{

	private BuildTarget buildTarget;
	private String profile = "default";
	private String version;
	
	private Path logFilePath;
	
	public MiniProgramStore(String dataRootPath, 
			String owner, 
			String repositoryName, 
			String projectName, 
			BuildTarget buildTarget,
			String version) {
		super(dataRootPath, owner, repositoryName, projectName);
		
		this.buildTarget = buildTarget;
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
			this.logFilePath = getProjectSourceRootDirectory()
					.resolve("buildLogs")
					.resolve(buildTarget.getKey())
					.resolve(profile)
					.resolve(logFileName);
		}
		return this.logFilePath;
	}

	private Path getProjectSourceRootDirectory() {
		return Paths.get(this.getDataRootPath())
				.resolve("sources")
				.resolve(this.getOwner())
				.resolve(this.getRepositoryName())
				.resolve(this.getProjectName());
	}

	private String generateLogFileName(String gitShortCommitId) {
		if(StringUtils.isBlank(gitShortCommitId)) {
			gitShortCommitId = StringUtils.repeat('0', 7);
		}
		return this.version + 
				"-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + 
				"-" + gitShortCommitId + ".log";
	}
	
	/**
	 * 获取存储项目模型的根目录
	 * 
	 * @return 项目模型根目录
	 */
	public Path getProjectModelDirectory() {
		return Paths.get(this.getDataRootPath())
				.resolve("models")
				.resolve(this.getOwner())
				.resolve(this.getRepositoryName())
				.resolve(this.getProjectName());
	}
	
	/**
	 * 获取存储项目源码的根目录，项目源码是根据项目模型生成的，并且不同的 AppType 和 profile 的源码不同
	 * 
	 * @return 项目源码根目录
	 */
	public Path getProjectSourceDirectory() {
		return getProjectSourceRootDirectory()
				.resolve("source")
				.resolve(buildTarget.getKey())
				.resolve(profile);
	}

}
