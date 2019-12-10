package com.blocklang.marketplace.data;

import javax.validation.constraints.NotBlank;

public class NewComponentRepoParam {

	@NotBlank(message = "{NotBlank.componentRepoGitUrl}")
	private String gitUrl;
	// 注意，本字段临时放在此处，当想明白系统如何处理因为配置项有误时，再移到合适的地方
	// 其中一个选项是在项目刚启动后，进行全面校验。
	private String propertyConfig;

	public String getGitUrl() {
		return gitUrl;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}

	public String getPropertyConfig() {
		return propertyConfig;
	}

	public void setPropertyConfig(String propertyConfig) {
		this.propertyConfig = propertyConfig;
	}
	
}
