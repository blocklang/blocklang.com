package com.blocklang.marketplace.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name = "api_service_request_body", 
	uniqueConstraints = { 
		@UniqueConstraint(columnNames = { "api_service_id", "code" }),
		@UniqueConstraint(columnNames = { "api_service_id", "name" }) 
	}
)
public class ApiServiceRequestBody extends PartialIdField {

	private static final long serialVersionUID = 7468693291041931295L;

	@Column(name = "api_service_id", nullable = false)
	private Integer apiServiceId;

	@Column(name = "code", nullable = false, length = 4)
	private String code;

	@Column(name = "name", nullable = false, length = 64)
	private String name;

	@Column(name = "description", length = 512)
	private String description;

	@Column(name = "value_type", nullable = false, length = 32)
	private String valueType;

	@Column(name = "api_service_schema_id")
	private Integer apiServiceSchemaId;

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

	public Integer getApiServiceId() {
		return apiServiceId;
	}

	public void setApiServiceId(Integer apiServiceId) {
		this.apiServiceId = apiServiceId;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public Integer getApiServiceSchemaId() {
		return apiServiceSchemaId;
	}

	public void setApiServiceSchemaId(Integer apiServiceSchemaId) {
		this.apiServiceSchemaId = apiServiceSchemaId;
	}

}
