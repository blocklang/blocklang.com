package com.blocklang.develop.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialOperateFields;

@Entity
@Table(name="project", uniqueConstraints = @UniqueConstraint(columnNames = { "project_name", "create_user_id" }))
public class Project extends PartialOperateFields{

	private static final long serialVersionUID = -3104661641464511556L;

	@Column(name = "project_name", length = 32, nullable = false)
	private String name;

	@Column(name = "project_desc", length = 128)
	private String description;

	@Column(name = "is_private", nullable = false)
	private Boolean isPrivate;

	@Column(name = "last_active_time", nullable = false)
	private LocalDateTime lastActiveTime;
	
	@Column(name="avatar_url", length = 256)
	private String avatarUrl;
	
	@Transient
	private String createUserName;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public LocalDateTime getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(LocalDateTime lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getCreateUserName() {
		return createUserName;
	}

	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}

}
