package com.blocklang.marketplace.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name = "api_service_response", 
	uniqueConstraints = { 
		@UniqueConstraint(columnNames = { "api_service_id", "code" }),
		@UniqueConstraint(columnNames = { "api_service_id", "name" }) 
	}
)
public class ApiServiceResponse extends PartialIdField {

	private static final long serialVersionUID = -7173682878924008702L;

	@Column(name = "api_repo_version_id", nullable = false)
	private Integer apiRepoVersionId;
	
	@Column(name = "api_service_id", nullable = false)
	private Integer apiServiceId;

	@Column(name = "code", nullable = false, length = 4)
	private String code;

	@Column(name = "name", nullable = false, length = 64)
	private String name;
	
	@Column(name = "status_code", nullable = false, length = 3)
	private String statusCode;
	
	@Column(name = "content_type", nullable = false, length = 64)
	private String contentType;

	@Column(name = "description", length = 512)
	private String description;

	@Column(name = "value_type", nullable = false, length = 32)
	private String valueType;

	@Column(name = "api_service_schema_id")
	private Integer apiServiceSchemaId;

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

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
