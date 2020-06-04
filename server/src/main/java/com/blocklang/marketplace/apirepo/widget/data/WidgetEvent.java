package com.blocklang.marketplace.apirepo.widget.data;

import java.util.List;

public class WidgetEvent {

	private String code;
	private String name;
	private String label;
	private String valueType;
	private String description;
	private List<WidgetEventArgument> arguments;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<WidgetEventArgument> getArguments() {
		return arguments;
	}

	public void setArguments(List<WidgetEventArgument> arguments) {
		this.arguments = arguments;
	}

}
