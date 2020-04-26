package com.blocklang.develop.designer.data;

import java.util.List;

public class RepoWidgetList {
	private Integer apiRepoId;
	private String apiRepoName;
	private List<WidgetCategory> widgetCategories;

	public Integer getApiRepoId() {
		return apiRepoId;
	}

	public void setApiRepoId(Integer apiRepoId) {
		this.apiRepoId = apiRepoId;
	}

	public String getApiRepoName() {
		return apiRepoName;
	}

	public void setApiRepoName(String apiRepoName) {
		this.apiRepoName = apiRepoName;
	}

	public List<WidgetCategory> getWidgetCategories() {
		return widgetCategories;
	}

	public void setWidgetCategories(List<WidgetCategory> widgetCategories) {
		this.widgetCategories = widgetCategories;
	}

}
