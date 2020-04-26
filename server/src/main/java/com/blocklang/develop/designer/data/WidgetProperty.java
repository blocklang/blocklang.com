package com.blocklang.develop.designer.data;

import java.util.List;

public class WidgetProperty {

	private String code;
	private String name;
	private String defaultValue;
	private String valueType;
	private List<EventArgument> arguments;

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

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public List<EventArgument> getArguments() {
		return arguments;
	}

	public void setArguments(List<EventArgument> arguments) {
		this.arguments = arguments;
	}

}
