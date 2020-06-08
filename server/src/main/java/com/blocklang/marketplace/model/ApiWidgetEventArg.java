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
@Table(name = "api_widget_event_arg", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "api_widget_prop_id", "code" })
	}
)
public class ApiWidgetEventArg extends PartialIdField{

	private static final long serialVersionUID = 1097230294560182509L;

	@Column(name = "api_widget_prop_id", nullable = false)
	private Integer apiWidgetPropertyId;
	
	@Column(name = "code", nullable = false, length = 4)
	private String code;
	
	@Column(name = "name", nullable = false, length = 32)
	private String name;
	
	@Column(name = "label", length = 32)
	private String label;
	
	@Convert(converter = WidgetPropertyValueTypeConverter.class)
	@Column(name = "value_type", nullable = false, length = 32)
	private WidgetPropertyValueType valueType;
	
	@Column(name = "default_value", length = 32)
	private String defaultValue;
	
	@Column(name = "description", length = 512)
	private String description;
	
	@Column(name = "seq", nullable = false)
	private Integer seq;

	public Integer getApiWidgetPropertyId() {
		return apiWidgetPropertyId;
	}

	public void setApiWidgetPropertyId(Integer apiWidgetPropertyId) {
		this.apiWidgetPropertyId = apiWidgetPropertyId;
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
