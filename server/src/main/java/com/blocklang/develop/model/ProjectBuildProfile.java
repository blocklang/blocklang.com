package com.blocklang.develop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.blocklang.core.model.PartialOperateFields;

@Entity
@Table(name = "project_build_profile", indexes = @Index(columnList = "project_id"))
public class ProjectBuildProfile extends PartialOperateFields{

	private static final long serialVersionUID = -2081259990102810146L;

	@Column(name = "project_id", nullable = false)
	private Integer projectId;
	
	@Column(name = "profile_name", length = 64, nullable = false)
	private String name;

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
