package com.blocklang.marketplace.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;
import com.blocklang.marketplace.constant.ComponentAttrValueType;
import com.blocklang.marketplace.constant.converter.ComponentAttrValueTypeConverter;

@Entity
@Table(name = "api_component_attr", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "api_component_id", "code" }),
		@UniqueConstraint(columnNames = { "api_component_id", "name" }) 
	}
)
public class ApiComponentAttr extends PartialIdField{

	private static final long serialVersionUID = -6776355043794409062L;

	@Column(name = "api_component_id", nullable = false)
	private String apiComponentId;
	
	@Column(name = "code", nullable = false, length = 4)
	private String code;
	
	@Column(name = "name", nullable = false, length = 64)
	private String name;
	
	@Column(name = "label", length = 64)
	private String label;
	
	@Column(name = "description", length = 512)
	private String description;
	
	@Convert(converter = ComponentAttrValueTypeConverter.class)
	@Column(name = "value_type", nullable = false, length = 32)
	private ComponentAttrValueType valueType;
	
	@Column(name = "value_description", length = 64)
	private String valueDescription;
	
	@Column(name = "default_value", length = 32)
	private String defaultValue;

	public String getApiComponentId() {
		return apiComponentId;
	}

	public void setApiComponentId(String apiComponentId) {
		this.apiComponentId = apiComponentId;
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

	public ComponentAttrValueType getValueType() {
		return valueType;
	}

	public void setValueType(ComponentAttrValueType valueType) {
		this.valueType = valueType;
	}

	public String getValueDescription() {
		return valueDescription;
	}

	public void setValueDescription(String valueDescription) {
		this.valueDescription = valueDescription;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
}
