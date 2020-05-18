package com.blocklang.marketplace.data.changelog;

import java.util.List;

public class WidgetProperty {

	private String code;
	private String name;
	private String label;
	private Object defaultValue; // defaultValue 的类型要与 valueType 指定的值匹配
	private String valueType;
	private String description;
	private List<WidgetPropertyOption> options;

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

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<WidgetPropertyOption> getOptions() {
		return options;
	}

	public void setOptions(List<WidgetPropertyOption> options) {
		this.options = options;
	}

}
