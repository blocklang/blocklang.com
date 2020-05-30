package com.blocklang.marketplace.apiparser.widget;

import java.util.List;

import com.blocklang.marketplace.apiparser.ChangeData;

public class AddWidgetPropertyData implements ChangeData{

	private List<WidgetProperty> properties;

	public List<WidgetProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<WidgetProperty> properties) {
		this.properties = properties;
	}
	
}
