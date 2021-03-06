package com.blocklang.develop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.blocklang.core.model.PartialOperateFields;

@Entity
@Table(name = "project_dependency", indexes = @Index(columnList = "project_id"))
public class ProjectDependency extends PartialOperateFields {

	private static final long serialVersionUID = -8084080967211641885L;
	
	@Column(name = "repository_id", nullable = false)
	private Integer repositoryId;
	
	@Column(name = "project_id", nullable = false)
	private Integer projectId;
	
	@Column(name = "component_repo_version_id", nullable = false)
	private Integer componentRepoVersionId;
	
	@Column(name = "project_build_profile_id")
	private Integer profileId;

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
