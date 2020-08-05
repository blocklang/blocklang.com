package com.blocklang.marketplace.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name = "api_widget_event_arg", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "api_widget_prop_id", "code" })
	}
)
public class ApiWidgetEventArg extends PartialIdField{

	private static final long serialVersionUID = 1097230294560182509L;

	@Column(name = "api_repo_version_id", nullable = false)
	private Integer apiRepoVersionId;
	
	@Column(name = "api_widget_prop_id", nullable = false)
	private Integer apiWidgetPropertyId;
	
	@Column(name = "code", nullable = false, length = 4)
	private String code;
	
	@Column(name = "name", nullable = false, length = 32)
	private String name;
	
	@Column(name = "label", length = 32)
	private String label;
	
	// 不使用 WidgetPropertyValueType 枚举，因为要支持用户自定义类型
	@Column(name = "value_type", nullable = false, length = 32)
	private String valueType;
	
	@Column(name = "default_value", length = 32)
	private String defaultValue;
	
	@Column(name = "description", length = 512)
	private String description;
	
	@Column(name = "seq", nullable = false)
	private Integer seq;

	public Integer getApiRepoVersionId() {
		return apiRepoVersionId;
	}

	public void setApiRepoVersionId(Integer apiRepoVersionId) {
		this.apiRepoVersionId = apiRepoVersionId;
	}

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

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
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
