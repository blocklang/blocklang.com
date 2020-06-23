package com.blocklang.marketplace.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialOperateFields;

@Entity
@Table(name = "api_jsobj", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "api_repo_version_id", "code" }),
		@UniqueConstraint(columnNames = { "api_repo_version_id", "name" }) 
	}
)
public class ApiJsObject extends PartialOperateFields{

	private static final long serialVersionUID = 552029345524883869L;

	@Column(name = "api_repo_version_id", nullable = false)
	private Integer apiRepoVersionId;
	
	@Column(name = "code", nullable = false, length = 4)
	private String code;
	
	@Column(name = "name", nullable = false, length = 64)
	private String name;
	
	@Column(name = "description", length = 512)
	private String description;

	public Integer getApiRepoVersionId() {
		return apiRepoVersionId;
	}

	public void setApiRepoVersionId(Integer apiRepoVersionId) {
		this.apiRepoVersionId = apiRepoVersionId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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

}