package com.blocklang.core.git;

import java.time.LocalDateTime;

public class GitFileInfo {

	private String path;
	private String name;
	private boolean isFolder;
	private String commitId;
	private LocalDateTime latestCommitTime;
	private String latestShortMessage;
	private String latestFullMessage;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isFolder() {
		return isFolder;
	}
	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}
	public String getCommitId() {
		return commitId;
	}
	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}
	public LocalDateTime getLatestCommitTime() {
		return latestCommitTime;
	}
	public void setLatestCommitTime(LocalDateTime latestCommitTime) {
		this.latestCommitTime = latestCommitTime;
	}
	public String getLatestShortMessage() {
		return latestShortMessage;
	}
	public void setLatestShortMessage(String latestShortMessage) {
		this.latestShortMessage = latestShortMessage;
	}
	public String getLatestFullMessage() {
		return latestFullMessage;
	}
	public void setLatestFullMessage(String latestFullMessage) {
		this.latestFullMessage = latestFullMessage;
	}
	public String getParentPath() {
		if(this.path == null || this.name == null) {
			return null;
		}
		if(this.path.equals(this.name)) {
			return "";
		}
		return this.path.substring(0, this.path.length() - (this.name.length() + 1/*多余的/*/));
	}
}
