package com.blocklang.marketplace.apirepo;

import java.util.List;

import com.blocklang.marketplace.data.RepoConfigJson;

public class RefData<T extends ApiObject> {

	private String gitUrl;
	private String fullRefName;
	private String shortRefName;
	private RepoConfigJson repoConfig;
	private List<T> apiObjects;
	private Integer createUserId;
	
	private boolean invalidData = false;

	public String getGitUrl() {
		return gitUrl;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}

	public String getFullRefName() {
		return fullRefName;
	}

	public void setFullRefName(String fullRefName) {
		this.fullRefName = fullRefName;
	}

	public String getShortRefName() {
		return shortRefName;
	}

	public void setShortRefName(String shortRefName) {
		this.shortRefName = shortRefName;
	}

	public RepoConfigJson getRepoConfig() {
		return repoConfig;
	}

	public void setRepoConfig(RepoConfigJson repoConfig) {
		this.repoConfig = repoConfig;
	}

	public List<T> getApiObjects() {
		return apiObjects;
	}

	public void setApiObjects(List<T> apiObjects) {
		this.apiObjects = apiObjects;
	}

	public Integer getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(Integer createUserId) {
		this.createUserId = createUserId;
	}

	public void readFailed() {
		this.invalidData = true;
	}

	public boolean isInvalidData() {
		return invalidData;
	}

}
