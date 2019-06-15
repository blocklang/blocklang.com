package com.blocklang.marketplace.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name = "component_repo_version", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "component_repo_id", "version" })
	}
)
public class ComponentRepoVersion extends PartialIdField {

	private static final long serialVersionUID = 5454786240466551849L;

	@Column(name = "component_repo_id", nullable = false)
	private Integer componentRepoId;
	
	@Column(name = "version", nullable = false, length = 32)
	private String version;

	@Column(name = "create_user_id", insertable = true, updatable = false, nullable = false)
	private Integer createUserId;
	
	@Column(name = "create_time", insertable = true, updatable = false, nullable = false)
	private LocalDateTime createTime;

	public Integer getComponentRepoId() {
		return componentRepoId;
	}

	public void setComponentRepoId(Integer componentRepoId) {
		this.componentRepoId = componentRepoId;
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
	
}
