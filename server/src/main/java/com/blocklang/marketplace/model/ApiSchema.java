package com.blocklang.marketplace.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.model.PartialOperateFields;

@Entity
@Table(name = "api_schema", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "api_repo_version_id", "parent_id", "name" }) })
public class ApiSchema extends PartialOperateFields {

	private static final long serialVersionUID = -6145790673297157010L;

	@Column(name = "api_repo_version_id", nullable = false)
	private Integer apiRepoVersionId;

	@Column(name = "name", length = 64)
	private String name;

	@Column(name = "type", nullable = false, length = 32)
	private String type;

	@Column(name = "description", length = 512)
	private String description;

	@Column(name = "parent_id", nullable = false)
	private Integer parentId = Constant.TREE_ROOT_ID;

	public Integer getApiRepoVersionId() {
		return apiRepoVersionId;
	}

	public void setApiRepoVersionId(Integer apiRepoVersionId) {
		this.apiRepoVersionId = apiRepoVersionId;
	}

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

}
