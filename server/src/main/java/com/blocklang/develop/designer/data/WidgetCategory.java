package com.blocklang.develop.designer.data;

import java.util.List;

import com.blocklang.marketplace.model.ApiComponent;

public class WidgetCategory {
	
	private String name;
	private List<ApiComponent> widgets;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ApiComponent> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<ApiComponent> widgets) {
		this.widgets = widgets;
	}

}
