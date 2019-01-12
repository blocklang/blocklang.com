package com.blocklang.release.model;

import java.time.LocalDateTime;

import com.blocklang.release.constant.ReleaseMethod;

public class AppRelease {
	private Integer id;
	private Integer appId;
	private String version;
	private String title;
	private String description;
	private LocalDateTime releaseTime;
	private ReleaseMethod releaseMethod;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

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

	public LocalDateTime getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(LocalDateTime releaseTime) {
		this.releaseTime = releaseTime;
	}

	public ReleaseMethod getReleaseMethod() {
		return releaseMethod;
	}

	public void setReleaseMethod(ReleaseMethod releaseMethod) {
		this.releaseMethod = releaseMethod;
	}

}
