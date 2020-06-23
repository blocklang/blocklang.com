package com.blocklang.core.util;

public class GitUrlSegment {
	private String website;
	private String owner;
	private String repoName;
	
	public GitUrlSegment(String website, String owner, String repoName) {
		this.website = website;
		this.owner = owner;
		this.repoName = repoName;
	}

	public String getWebsite() {
		return website;
	}

	public String getOwner() {
		return owner;
	}

	public String getRepoName() {
		return repoName;
	}

	/**
	 * 一个完整的 gitUrl 示例: https://github.com/blocklang/blocklang.com.git
	 * 
	 * @param gitRemoteUrl
	 * @return
	 */
	public static GitUrlSegment of(String gitRemoteUrl) {
		if(org.apache.commons.lang3.StringUtils.isBlank(gitRemoteUrl)) {
			return null;
		}

		String url = gitRemoteUrl.toLowerCase();
		
		if(!url.startsWith("https://")) {
			return null;
		}
		
		if(!url.endsWith(".git")) {
			return null;
		}
		
		// 1. 去除开头的 https://
		url = url.substring("https://".length());
		
		// 2. 去除结尾的 .git
		url = url.substring(0, url.length() - ".git".length());
		String[] segments = url.split("/");
		
		if(segments.length != 3) {
			return null;
		}
		
		return new GitUrlSegment(segments[0], segments[1], segments[2]);
	}
}
