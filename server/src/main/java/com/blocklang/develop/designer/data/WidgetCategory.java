package com.blocklang.develop.designer.data;

import java.util.List;

public class WidgetCategory {
	
	private String name;
	private List<Widget> widgets;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Widget> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<Widget> widgets) {
		this.widgets = widgets;
	}

}
