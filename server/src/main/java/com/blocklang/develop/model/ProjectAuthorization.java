package com.blocklang.develop.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.blocklang.core.model.PartialIdField;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.converter.AccessLevelConverter;

@Entity
@Table(name = "project_authorization")
public class ProjectAuthorization extends PartialIdField{

	private static final long serialVersionUID = -6231422105829149918L;
	
	@Column(name="user_id", nullable = false)
	private Integer userId;
	
	@Column(name = "project_id", nullable = false)
	private Integer projectId;
	
	@Convert(converter = AccessLevelConverter.class)
	@Column(name="access_level", nullable = false, length = 2)
	private AccessLevel accessLevel;
	
	@Column(name = "create_user_id", insertable = true, updatable = false, nullable = false)
	private Integer createUserId;
	
	@Column(name = "create_time", insertable = true, updatable = false, nullable = false)
	private LocalDateTime createTime;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public AccessLevel getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(AccessLevel accessLevel) {
		this.accessLevel = accessLevel;
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
	
}
