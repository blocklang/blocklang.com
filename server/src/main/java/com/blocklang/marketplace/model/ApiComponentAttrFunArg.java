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
@Table(name = "api_component_attr_fun_arg", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "api_component_attr_id", "code" })
	}
)
public class ApiComponentAttrFunArg extends PartialIdField{

	private static final long serialVersionUID = 1097230294560182509L;

	@Column(name = "api_component_attr_id", nullable = false)
	private Integer apiComponentAttrId;
	
	@Column(name = "code", nullable = false, length = 4)
	private String code;
	
	@Column(name = "name", nullable = false, length = 32)
	private String name;
	
	@Column(name = "label", length = 32)
	private String label;
	
	@Convert(converter = ComponentAttrValueTypeConverter.class)
	@Column(name = "value_type", nullable = false, length = 32)
	private ComponentAttrValueType valueType;
	
	@Column(name = "default_value", length = 32)
	private String defaultValue;
	
	@Column(name = "description", length = 512)
	private String description;
	
	@Column(name = "seq", nullable = false)
	private Integer seq;

	public Integer getApiComponentAttrId() {
		return apiComponentAttrId;
	}

	public void setApiComponentAttrId(Integer apiComponentAttrId) {
		this.apiComponentAttrId = apiComponentAttrId;
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

	public ComponentAttrValueType getValueType() {
		return valueType;
	}

	public void setValueType(ComponentAttrValueType valueType) {
		this.valueType = valueType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

}
