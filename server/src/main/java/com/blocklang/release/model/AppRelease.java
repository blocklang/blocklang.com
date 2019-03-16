package com.blocklang.release.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.release.constant.ReleaseMethod;
import com.blocklang.release.constant.converter.ReleaseMethodConverter;

@Entity
@Table(name = "app_release", uniqueConstraints = @UniqueConstraint(columnNames = { "app_id", "version" }))
public class AppRelease extends PartialOperateFields{

	private static final long serialVersionUID = 7900341616960175943L;
	
	@Column(name = "app_id", nullable = false)
	private Integer appId;
	
	@Column(nullable = false, length = 32)
	private String version;
	
	@Column(nullable = false, length = 64)
	private String title;
	
	private String description;
	
	@Column(name = "release_time", nullable = false)
	private LocalDateTime releaseTime;
	
	@Convert(converter = ReleaseMethodConverter.class)
	@Column(name = "release_method", length = 2, nullable = false)
	private ReleaseMethod releaseMethod;
	
	@Transient
	private String name; // app name

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
