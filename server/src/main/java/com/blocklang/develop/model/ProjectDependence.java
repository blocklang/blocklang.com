package com.blocklang.develop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.blocklang.core.model.PartialOperateFields;

@Entity
@Table(name = "project_dependence", indexes = @Index(columnList = "project_id"))
public class ProjectDependence extends PartialOperateFields {

	private static final long serialVersionUID = -8084080967211641885L;
	
	@Column(name = "project_id", nullable = false)
	private Integer projectId;
	
	@Column(name = "component_repo_version_id", nullable = false)
	private Integer componentRepoVersionId;
	
	@Column(name = "project_build_profile_id")
	private Integer profileId;

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public Integer getComponentRepoVersionId() {
		return componentRepoVersionId;
	}

	public void setComponentRepoVersionId(Integer componentRepoVersionId) {
		this.componentRepoVersionId = componentRepoVersionId;
	}

	public Integer getProfileId() {
		return profileId;
	}

	public void setProfileId(Integer profileId) {
		this.profileId = profileId;
	}
	
}
