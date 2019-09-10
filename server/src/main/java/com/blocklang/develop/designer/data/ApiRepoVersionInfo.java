package com.blocklang.develop.designer.data;

import com.blocklang.marketplace.constant.RepoCategory;

public class ApiRepoVersionInfo {

	private Integer apiRepoId;
	private String apiRepoName;
	private RepoCategory category;
	
	private Integer apiRepoVersionId;

	public Integer getApiRepoId() {
		return apiRepoId;
	}

	public void setApiRepoId(Integer apiRepoId) {
		this.apiRepoId = apiRepoId;
	}

	public String getApiRepoName() {
		return apiRepoName;
	}

	public void setApiRepoName(String apiRepoName) {
		this.apiRepoName = apiRepoName;
	}

	public RepoCategory getCategory() {
		return category;
	}

	public void setCategory(RepoCategory category) {
		this.category = category;
	}

	public Integer getApiRepoVersionId() {
		return apiRepoVersionId;
	}

	public void setApiRepoVersionId(Integer apiRepoVersionId) {
		this.apiRepoVersionId = apiRepoVersionId;
	}
	
}
