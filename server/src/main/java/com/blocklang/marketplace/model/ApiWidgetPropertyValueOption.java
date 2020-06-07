package com.blocklang.marketplace.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name = "api_widget_prop_val_opt", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "api_widget_prop_id", "code" })
	}
)
public class ApiWidgetPropertyValueOption extends PartialIdField{

	private static final long serialVersionUID = 1097230294560182509L;

	@Column(name = "api_widget_prop_id", nullable = false)
	private Integer apiWidgetPropertyId;
	
	@Column(name = "code", nullable = false, length = 4)
	private String code;
	
	@Column(name = "value", nullable = false, length = 32)
	private String value;
	
	@Column(name = "label", length = 32)
	private String label;
	
	@Column(name = "description", length = 512)
	private String description;
	
	@Column(name = "value_description", length = 512)
	private String valueDescription;

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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

	public String getValueDescription() {
		return valueDescription;
	}

	public void setValueDescription(String valueDescription) {
		this.valueDescription = valueDescription;
	}

}
