package com.blocklang.release.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.constant.converter.ReleaseResultConverter;

@Entity
@Table(name = "project_release_task", uniqueConstraints = @UniqueConstraint(columnNames = { "project_id", "version" }))
public class ProjectReleaseTask extends PartialOperateFields{

	private static final long serialVersionUID = -2695309681346878878L;

	@Column(name = "project_id", nullable = false)
	private Integer projectId;
	
	@Column(name = "version", nullable = false, length = 32)
	private String version;
	
	@Column(nullable = false, length = 64)
	private String title;
	
	private String description;
	
	@Column(name = "jdk_app_id", nullable = false)
	private Integer jdkAppId;
	
	@Column(name = "start_time", nullable = false)
	private LocalDateTime startTime;
	
	@Column(name = "end_time")
	private LocalDateTime endTime;
	
	@Convert(converter = ReleaseResultConverter.class)
	@Column(name = "release_result", length = 2, nullable = false)	
	private ReleaseResult releaseResult;

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
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

	public Integer getJdkAppId() {
		return jdkAppId;
	}

	public void setJdkAppId(Integer jdkAppId) {
		this.jdkAppId = jdkAppId;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public ReleaseResult getReleaseResult() {
		return releaseResult;
	}

	public void setReleaseResult(ReleaseResult releaseResult) {
		this.releaseResult = releaseResult;
	}
	
}
