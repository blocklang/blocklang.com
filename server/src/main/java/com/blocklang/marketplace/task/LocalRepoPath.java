package com.blocklang.marketplace.task;

import java.nio.file.Path;

/**
 * git 仓库在本地(部署 blocklang 的服务器)上的信息
 * 
 * @author Zhengwei Jin
 *
 */
public class LocalRepoPath {

	private String dataRootPath; // block lang 站点的项目文件根目录
	private String website;
	private String owner;
	private String repoName;
	
	private String gitUrl;
	
	public LocalRepoPath(String dataRootPath, String gitUrl) {
		this.dataRootPath = dataRootPath;
		this.gitUrl = gitUrl;
		this.parse(gitUrl);
	}
	
	private void parse(String gitUrl) {
		// 一个完整的 gitUrl 示例
		// https://github.com/blocklang/blocklang.com.git
		String url = gitUrl.toLowerCase();
		// 1. 去除开头的 https://
		if(url.startsWith("https://")) {
			url = url.substring("https://".length());
		}
		// 2. 去除结尾的 .git
		if(url.endsWith(".git")) {
			url = url.substring(0, url.length() - ".git".length());
		}
		String[] segments = url.split("/");
		
		this.website = segments[0];
		this.owner = segments[1];
		this.repoName = segments[2];
	}

	public Path getRepoSourceDirectory() {
		return this.getRepoRootDirectory().resolve("source");
	}
	
	public Path getRepoRootDirectory() {
		return Path.of(this.dataRootPath, "marketplace", this.website, this.owner, this.repoName);
	}

	public String getWebsite() {
		return this.website;
	}
	
	public String getGitUrl() {
		return this.gitUrl;
	}

	public String getOwner() {
		return this.owner;
	}

	public String getRepoName() {
		return this.repoName;
	}
}
