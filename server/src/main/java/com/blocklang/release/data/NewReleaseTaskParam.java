package com.blocklang.release.data;

import javax.validation.constraints.NotBlank;

/**
 * 创建发行版时使用的参数
 * 
 * @author Zhengwei Jin
 *
 */
public class NewReleaseTaskParam {

	@NotBlank(message = "{NotBlank.version}")
	private String version;
	@NotBlank(message = "{NotBlank.releaseTitle}")
	private String title;
	private String description;
	// 如果用户没有设置，则获取系统支持的最新 jdk
	//@NotNull(message = "{NotNull.jdkReleaseId}")
	private Integer jdkReleaseId;
	private String buildTarget;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getJdkReleaseId() {
		return jdkReleaseId;
	}

	public void setJdkReleaseId(Integer jdkReleaseId) {
		this.jdkReleaseId = jdkReleaseId;
	}

	public String getBuildTarget() {
		return buildTarget;
	}

	public void setBuildTarget(String buildTarget) {
		this.buildTarget = buildTarget;
	}

	@Override
	public String toString() {
		return "NewReleaseParam [version=" + version + ", title=" + title + ", description=" + description
				+ ", jdkReleaseId=" + jdkReleaseId + "]";
	}

}
