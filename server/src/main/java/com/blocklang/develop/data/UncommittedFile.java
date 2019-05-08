package com.blocklang.develop.data;

import com.blocklang.core.constant.GitFileStatus;

public class UncommittedFile {

	private String fullKeyPath; // 由 path 组成的完整路径
	private GitFileStatus gitStatus;
	private String icon;
	private String resourceName;
	private String parentNamePath;
	
	public String getFullKeyPath() {
		return fullKeyPath;
	}

	public void setFullKeyPath(String fullKeyPath) {
		this.fullKeyPath = fullKeyPath;
	}

	public GitFileStatus getGitStatus() {
		return gitStatus;
	}

	public void setGitStatus(GitFileStatus gitStatus) {
		this.gitStatus = gitStatus;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getParentNamePath() {
		return parentNamePath;
	}

	public void setParentNamePath(String parentNamePath) {
		this.parentNamePath = parentNamePath;
	}

}
