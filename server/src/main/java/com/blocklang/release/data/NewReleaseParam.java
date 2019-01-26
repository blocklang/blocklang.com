package com.blocklang.release.data;

import javax.validation.constraints.NotBlank;

/**
 * 创建发行版时使用的参数
 * 
 * @author Zhengwei Jin
 *
 */
public class NewReleaseParam {

	@NotBlank(message = "{NotBlank.version}")
	private String version;
	@NotBlank(message = "{NotBlank.releaseName}")
	private String name;
	private String description;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "NewReleaseParam [version=" + version + ", name=" + name + ", description=" + description + "]";
	}

}
