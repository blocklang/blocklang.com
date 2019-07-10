package com.blocklang.marketplace.data;

import java.nio.file.Path;

import com.blocklang.core.util.GitUrlParser;
import com.blocklang.core.util.GitUrlSegment;

/**
 * git 仓库在本地(部署 blocklang 的服务器)上的信息
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
		this.gitUrlSegment = GitUrlParser.parse(gitUrl);
	}

	public Path getRepoSourceDirectory() {
		return this.getRepoRootDirectory().resolve("source");
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
