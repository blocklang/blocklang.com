package com.blocklang.marketplace.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;
import com.blocklang.marketplace.constant.WidgetPropertyValueType;
import com.blocklang.marketplace.constant.converter.WidgetPropertyValueTypeConverter;

@Entity
@Table(name = "api_widget_prop", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "api_widget_id", "code" }),
		@UniqueConstraint(columnNames = { "api_widget_id", "name" }) 
	}
)
public class ApiWidgetProperty extends PartialIdField{

	private static final long serialVersionUID = -6776355043794409062L;

	@Column(name = "api_repo_version_id", nullable = false)
	private Integer apiRepoVersionId;
	
	@Column(name = "api_widget_id", nullable = false)
	private Integer apiWidgetId;
	
	@Column(name = "code", nullable = false, length = 4)
	private String code;
	
	@Column(name = "name", nullable = false, length = 64)
	private String name;
	
	@Column(name = "label", length = 64)
	private String label;
	
	@Column(name = "description", length = 512)
	private String description;
	
	@Convert(converter = WidgetPropertyValueTypeConverter.class)
	@Column(name = "value_type", nullable = false, length = 32)
	private WidgetPropertyValueType valueType;

	@Column(name = "default_value", length = 32)
	private String defaultValue;
	
	@Column(name = "required")
	private Boolean required = false;

	public Integer getApiRepoVersionId() {
		return apiRepoVersionId;
	}

	public void setApiRepoVersionId(Integer apiRepoVersionId) {
		this.apiRepoVersionId = apiRepoVersionId;
	}

	public Integer getApiWidgetId() {
		return apiWidgetId;
	}

	public void setApiWidgetId(Integer apiWidgetId) {
		this.apiWidgetId = apiWidgetId;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public WidgetPropertyValueType getValueType() {
		return valueType;
	}

	public void setValueType(WidgetPropertyValueType valueType) {
		this.valueType = valueType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}
	
}
