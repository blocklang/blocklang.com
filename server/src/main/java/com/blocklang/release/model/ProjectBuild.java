package com.blocklang.release.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.blocklang.release.constant.BuildResult;
import com.blocklang.release.constant.converter.BuildResultConverter;

@Entity
@Table(name = "project_build")
public class ProjectBuild extends PartialOperateFields {

	private static final long serialVersionUID = -6727998479607575646L;

	@Column(name = "project_tag_id", nullable = false, unique = true)
	private Integer projectTagId;
	
	@Column(name = "start_time", nullable = false)
	private LocalDateTime startTime;
	
	@Column(name = "end_time")
	private LocalDateTime endTime;
	
	@Convert(converter = BuildResultConverter.class)
	@Column(name = "build_result", length = 2, nullable = false)	
	private BuildResult buildResult;

	public Integer getProjectTagId() {
		return projectTagId;
	}

	public void setProjectTagId(Integer projectTagId) {
		this.projectTagId = projectTagId;
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

	public BuildResult getBuildResult() {
		return buildResult;
	}

	public void setBuildResult(BuildResult buildResult) {
		this.buildResult = buildResult;
	}
	
}
