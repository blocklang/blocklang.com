package com.blocklang.marketplace.data;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.blocklang.core.util.GitUrlSegment;

/**
 * 在往组件市场注册组件时，会生成一个文件。该类用于描述目录结构
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

	public Path getRepoSourceDirectory() {
		return this.getRepoRootDirectory().resolve("source");
	}
	
	public Path getRepoPackageDirectory() {
		return this.getRepoRootDirectory().resolve("package");
	}
	
	public Path getRepoBuildDirectory() {
		return this.getRepoRootDirectory().resolve("build");
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

}
