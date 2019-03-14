package com.blocklang.release.data;

import javax.validation.constraints.NotBlank;

/**
 * 校验发布版本号
 * 
 * @author Zhengwei Jin
 *
 */
public class CheckReleaseVersionParam {

	@NotBlank(message = "{NotBlank.releaseVersion}")
	private String version;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
