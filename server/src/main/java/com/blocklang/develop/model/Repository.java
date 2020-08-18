package com.blocklang.develop.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.develop.constant.AccessLevel;

@Entity
@Table(name="repository", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "create_user_id" }))
public class Repository extends PartialOperateFields{

	private static final long serialVersionUID = -3104661641464511556L;

	@Column(name = "name", length = 32, nullable = false)
	private String name;

	@Column(name = "description", length = 128)
	private String description;

	@Column(name = "is_public", nullable = false)
	private Boolean isPublic;

	@Column(name = "last_active_time", nullable = false)
	private LocalDateTime lastActiveTime;
	
	@Column(name="avatar_url", length = 256)
	private String avatarUrl;
	
	// 创建用户的登录名，createUserId 对应的用户名
	@Transient
	private String createUserName;
	
	@Transient
	private AccessLevel accessLevel;

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

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
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

	public void setAccessLevel(AccessLevel accessLevel) {
		this.accessLevel = accessLevel;
	}

	public AccessLevel getAccessLevel() {
		return accessLevel;
	}

}
