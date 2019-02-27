package com.blocklang.develop.data;

import java.time.LocalDateTime;

public class GitCommitInfo {

	private String id;
	private LocalDateTime commitTime;
	private String shortMessage;
	private String fullMessage;
	private String userName;
	private String avatarUrl;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocalDateTime getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(LocalDateTime commitTime) {
		this.commitTime = commitTime;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public String getFullMessage() {
		return fullMessage;
	}

	public void setFullMessage(String fullMessage) {
		this.fullMessage = fullMessage;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
}
