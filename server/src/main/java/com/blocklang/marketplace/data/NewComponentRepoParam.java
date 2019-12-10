package com.blocklang.marketplace.data;

import javax.validation.constraints.NotBlank;

public class NewComponentRepoParam {

	@NotBlank(message = "{NotBlank.componentRepoGitUrl}")
	private String gitUrl;

	public String getGitUrl() {
		return gitUrl;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}
}
