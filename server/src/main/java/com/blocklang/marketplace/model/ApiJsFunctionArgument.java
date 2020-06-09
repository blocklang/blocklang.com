package com.blocklang.marketplace.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name = "api_jsobj_func_arg", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "api_jsobj_func_id", "code" }) })
public class ApiJsFunctionArgument extends PartialIdField {

	private static final long serialVersionUID = -7111841175642612995L;

	@Column(name = "api_jsobj_func_id", nullable = false)
	private Integer apiJsFunctionId;

	@Column(name = "code", nullable = false, length = 4)
	private String code;

	@Column(name = "name", nullable = false, length = 32)
	private String name;

	@Column(name = "type", nullable = false, length = 32)
	private String type;

	@Column(name = "optional")
	private Boolean optional = false;

	@Column(name = "variable")
	private Boolean variable;

	@Column(name = "seq", nullable = false)
	private Integer seq;

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

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public Integer getApiJsFunctionId() {
		return apiJsFunctionId;
	}

	public void setApiJsFunctionId(Integer apiJsFunctionId) {
		this.apiJsFunctionId = apiJsFunctionId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getOptional() {
		return optional;
	}

	public void setOptional(Boolean optional) {
		this.optional = optional;
	}

	public Boolean getVariable() {
		return variable;
	}

	public void setVariable(Boolean variable) {
		this.variable = variable;
	}

}