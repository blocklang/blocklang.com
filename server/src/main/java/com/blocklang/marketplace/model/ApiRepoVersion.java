package com.blocklang.marketplace.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name = "api_repo_version", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "api_repo_id", "version" })
	}
)
public class ApiRepoVersion extends PartialIdField{

	private static final long serialVersionUID = 5454786240466551849L;

	@Column(name = "api_repo_id", nullable = false)
	private Integer apiRepoId;
	
	@Column(name = "version", nullable = false, length = 32)
	private String version;

	@Column(name = "git_tag_name", nullable = false, length = 32)
	private String gitTagName;
	
	@Column(name = "name", nullable = false, length = 64)
	private String name;

	@Column(name = "display_name", length = 64)
	private String displayName;

	@Column(name = "description", length = 512)
	private String description;
	
	@Column(name = "last_publish_time" )
	private LocalDateTime lastPublishTime;

	@Column(name = "create_user_id", insertable = true, updatable = false, nullable = false)
	private Integer createUserId;
	
	@Column(name = "create_time", insertable = true, updatable = false, nullable = false)
	private LocalDateTime createTime;

	public Integer getApiRepoId() {
		return apiRepoId;
	}

	public void setApiRepoId(Integer apiRepoId) {
		this.apiRepoId = apiRepoId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Integer getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(Integer createUserId) {
		this.createUserId = createUserId;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public String getGitTagName() {
		return gitTagName;
	}

	public void setGitTagName(String gitTagName) {
		this.gitTagName = gitTagName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public LocalDateTime getLastPublishTime() {
		return lastPublishTime;
	}

	public void setLastPublishTime(LocalDateTime lastPublishTime) {
		this.lastPublishTime = lastPublishTime;
	}
}
