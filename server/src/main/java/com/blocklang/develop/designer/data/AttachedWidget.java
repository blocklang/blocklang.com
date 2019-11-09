package com.blocklang.develop.designer.data;

import java.util.Collections;
import java.util.List;

public class AttachedWidget {

	private String id;
	private String parentId;
	private Integer widgetId;
	private String widgetCode;
	private String widgetName;
	private Boolean canHasChildren;
	private Integer apiRepoId;
	private List<AttachedWidgetProperty> properties;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Integer getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(Integer widgetId) {
		this.widgetId = widgetId;
	}

	public String getWidgetCode() {
		return widgetCode;
	}

	public void setWidgetCode(String widgetCode) {
		this.widgetCode = widgetCode;
	}

	public String getWidgetName() {
		return widgetName;
	}

	public void setWidgetName(String widgetName) {
		this.widgetName = widgetName;
	}

	public Boolean getCanHasChildren() {
		return canHasChildren;
	}

	public void setCanHasChildren(Boolean canHasChildren) {
		this.canHasChildren = canHasChildren;
	}

	public Integer getApiRepoId() {
		return apiRepoId;
	}

	public void setApiRepoId(Integer apiRepoId) {
		this.apiRepoId = apiRepoId;
	}

	public List<AttachedWidgetProperty> getProperties() {
		return properties == null? Collections.emptyList():properties;
	}

	public void setProperties(List<AttachedWidgetProperty> properties) {
		this.properties = properties;
	}
	
}
