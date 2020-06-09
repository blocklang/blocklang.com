package com.blocklang.marketplace.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name = "api_jsobj_func", uniqueConstraints = { @UniqueConstraint(columnNames = { "api_jsobj_id", "code" }),
		@UniqueConstraint(columnNames = { "api_jsobj_id", "name" }) })
public class ApiJsFunction extends PartialIdField {

	private static final long serialVersionUID = -2920643274364350310L;

	@Column(name = "api_jsobj_id", nullable = false)
	private Integer apiJsObjectId;

	@Column(name = "code", nullable = false, length = 4)
	private String code;

	@Column(name = "name", nullable = false, length = 64)
	private String name;

	@Column(name = "return_type", length = 32)
	private String returnType;

	@Column(name = "description", length = 512)
	private String description;

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

	public Integer getApiJsObjectId() {
		return apiJsObjectId;
	}

	public void setApiJsObjectId(Integer apiJsObjectId) {
		this.apiJsObjectId = apiJsObjectId;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

}