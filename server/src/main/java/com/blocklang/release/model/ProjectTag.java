package com.blocklang.release.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialOperateFields;

@Entity
@Table(name = "project_tag", uniqueConstraints = @UniqueConstraint(columnNames = { "project_id", "version" }))
public class ProjectTag extends PartialOperateFields {

	private static final long serialVersionUID = -5846683023432275794L;

	@Column(name = "project_id", nullable = false)
	private Integer projectId;
	
	@Column(name = "version", nullable = false, length = 32)
	private String version;
	
	@Column(name = "git_tag_id", nullable = false, length = 50)
	private String gitTagId;

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

	public String getGitTagId() {
		return gitTagId;
	}

	public void setGitTagId(String gitTagId) {
		this.gitTagId = gitTagId;
	}

}
