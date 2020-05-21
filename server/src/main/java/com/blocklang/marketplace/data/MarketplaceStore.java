package com.blocklang.marketplace.data;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.blocklang.core.util.GitUrlSegment;

/**
 * 在往组件市场注册组件时，会生成一个文件。该类用于描述目录结构
 * 
 * IDE 版组件仓库的结构
 * 
 * <pre>
 * marketplace
 *     {website}
 *         {owner}
 *             {project_name}
 *                 source
 *                 publishLogs
 *                     yyyy_MM_dd_HH_mm_ss.log
 *                 build
 *                 package
 *                     {version}
 *                         main.bundle.js
 *                         
 * </pre>
 * 
 * API 版仓库的结构
 * 
 * <pre>
 * marketplace
 *     {website}
 *         {owner}
 *             {project_name}
 *                 source
 *                 publishLogs
 *                     yyyy_MM_dd_HH_mm_ss.log
 *                 build
 *                 package
 *                     {version}
 *                         {widget_timestamp}
 *                             index.json
 *                     __changelog__
 *                         {widget_timestamp}
 *                             index.json
 *                         
 * </pre>
 * 
 * @author Zhengwei Jin
 *
 */
public class MarketplaceStore {
	
	private static final String DIR_NAME_MARKETPLACE = "marketplace";
	private static final String DIR_NAME_PUBLISH_LOGS = "publishLogs";

	private Path rootPath;
	private GitUrlSegment gitUrlSegment;
	
	private Path logFile;
	
	
	/**
	 * 创建一个描述组件市场存储结构的对象
	 * 
	 * @param rootPath      根目录，值为绝对路径
	 * @param gitRemoteUrl  远程 git 仓库地址，如 https://github.com/blocklang/blocklang.com.git，注意，使用 https 协议，且以 .git 结尾
	 */
	public MarketplaceStore(String rootPath, String gitRemoteUrl) {
		this.rootPath = Path.of(rootPath);
		this.gitUrlSegment = GitUrlSegment.of(gitRemoteUrl);
	}
	
	public Path getLogFilePath() {
		if(this.logFile == null) {
			String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + ".log";
			this.logFile = getRepoRootDirectory()
					.resolve(DIR_NAME_PUBLISH_LOGS)
					.resolve(fileName);
		}
		
		return this.logFile;
	}
	
	public String getLogFileName() {
		return this.getLogFilePath().getFileName().toString();
	}

	public Path getRepoSourceDirectory() {
		return this.getRepoRootDirectory().resolve("source");
	}
	
	public Path getRepoPackageDirectory() {
		return this.getRepoRootDirectory().resolve("package");
	}
	
	public Path getRepoBuildDirectory() {
		return this.getRepoRootDirectory().resolve("build");
	}
	
	public Path getPackageChangeLogDirectory() {
		return this.getRepoPackageDirectory().resolve("__changelog__");
	}

	private Path getRepoRootDirectory() {
		return rootPath.resolve(DIR_NAME_MARKETPLACE)
				.resolve(gitUrlSegment.getWebsite())
				.resolve(gitUrlSegment.getOwner())
				.resolve(gitUrlSegment.getRepoName());
	}

	public Path getPackageVersionDirectory(String version) {
		return this.getRepoPackageDirectory()
				.resolve(version);
	}

	/**
	 * 获取仓库中的 blocklang.json 文件路径。
	 * 
	 * <p>
	 * 因为文件是存在 git 仓库中的，有多个版本，这里是直接使用 File API，而不是 Jgit API，来直接获取当前分支下的文件内容。
	 * 暗含的意思，就是以最新版本的仓库信息为准。
	 * </p>
	 * 
	 * @return blocklang.json 文件路径。
	 */
	public Path getRepoConfigFile() {
		return this.getRepoSourceDirectory().resolve("blocklang.json");
	}

}
