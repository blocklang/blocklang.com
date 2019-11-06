package com.blocklang.develop.designer.data;

public class Widget {

	private Integer widgetId;
	private String widgetName;
	private String widgetCode;
	private Boolean canHasChildren;
	private Integer apiRepoId;

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

}
