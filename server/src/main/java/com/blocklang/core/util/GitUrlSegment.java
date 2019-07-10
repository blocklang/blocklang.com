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
}
