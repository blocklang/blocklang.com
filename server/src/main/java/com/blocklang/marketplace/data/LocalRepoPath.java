package com.blocklang.marketplace.data;

import java.nio.file.Path;

import com.blocklang.core.util.GitUrlSegment;

/**
 * git 仓库在本地(部署 blocklang 的服务器)上的信息
 * 
 * TODO: 重命名为 MarketplaceStore，因为其中不仅仅存储 git 仓库信息
 * 
 * @author Zhengwei Jin
 *
 */
public class LocalRepoPath {

	private String dataRootPath; // block lang 站点的项目文件根目录
	private String gitUrl;
	private GitUrlSegment gitUrlSegment;
	
	public LocalRepoPath(String dataRootPath, String gitUrl) {
		this.dataRootPath = dataRootPath;
		this.gitUrl = gitUrl;
		this.gitUrlSegment = GitUrlSegment.of(gitUrl).orElse(null);
	}
	
	public LocalRepoPath(String dataRootPath, String repoWebsite, String repoOwner, String repoName) {
		this.dataRootPath = dataRootPath;
		this.gitUrlSegment = new GitUrlSegment(repoWebsite, repoOwner, repoName);
	}

	@Deprecated
	public Path getRepoSourceDirectory() {
		return this.getRepoRootDirectory().resolve("source");
	}
	
	@Deprecated
	public Path getRepoBuildDirectory() {
		return this.getRepoRootDirectory().resolve("build");
	}
	
	@Deprecated
	public Path getRepoPackageDirectory() {
		return this.getRepoRootDirectory().resolve("package");
	}
	
	public Path getRepoRootDirectory() {
		return Path.of(this.dataRootPath, 
				"marketplace", 
				gitUrlSegment.getWebsite(), 
				gitUrlSegment.getOwner(), 
				gitUrlSegment.getRepoName());
	}

	public String getGitUrl() {
		return this.gitUrl;
	}
	
	public String getWebsite() {
		return gitUrlSegment.getWebsite();
	}

	public String getOwner() {
		return gitUrlSegment.getOwner();
	}

	public String getRepoName() {
		return gitUrlSegment.getRepoName();
	}
}
