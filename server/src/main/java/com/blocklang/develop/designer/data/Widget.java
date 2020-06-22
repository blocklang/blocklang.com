package com.blocklang.develop.designer.data;

import java.util.Collections;
import java.util.List;

public class Widget {

	private Integer widgetId;
	private String widgetName;
	private String widgetCode;
	private Integer apiRepoId;
	private List<WidgetProperty> properties;

	public Integer getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(Integer widgetId) {
		this.widgetId = widgetId;
	}

	public String getWidgetName() {
		return widgetName;
	}

	public void setWidgetName(String widgetName) {
		this.widgetName = widgetName;
	}

	public String getWidgetCode() {
		return widgetCode;
	}

	public void setWidgetCode(String widgetCode) {
		this.widgetCode = widgetCode;
	}

	public Integer getApiRepoId() {
		return apiRepoId;
	}

	public void setApiRepoId(Integer apiRepoId) {
		this.apiRepoId = apiRepoId;
	}

	public List<WidgetProperty> getProperties() {
		return properties == null ? Collections.emptyList() : properties;
	}

	public void setProperties(List<WidgetProperty> properties) {
		this.properties = properties;
	}

}
