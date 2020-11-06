package com.blocklang.release.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.develop.constant.BuildTarget;
import com.blocklang.release.constant.converter.ReleaseResultConverter;

@Entity
@Table(name = "project_release")
public class ProjectRelease extends PartialOperateFields {

	private static final long serialVersionUID = 8023467184734591240L;

	@Column(name = "repository_id", nullable = false)
	private Integer repositoryId;

	@Column(name = "project_id", nullable = false)
	private Integer projectId;
	
	@Column(name = "git_commit_id", nullable = false)
	private String commitId;
	
	@Column(name = "version", length = 32, nullable = false)
	private String version;
	
	@Convert(converter = ReleaseResultConverter.class)
	@Column(name = "build_target")
	private BuildTarget buildTarget;

	public Integer getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId) {
		this.repositoryId = repositoryId;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public BuildTarget getBuildTarget() {
		return buildTarget;
	}

	public void setBuildTarget(BuildTarget buildTarget) {
		this.buildTarget = buildTarget;
	}
	
}
