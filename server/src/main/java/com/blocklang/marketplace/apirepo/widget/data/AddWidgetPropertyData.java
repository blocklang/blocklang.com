package com.blocklang.marketplace.apirepo.widget.data;

import java.util.List;

import com.blocklang.marketplace.apirepo.ChangeData;

public class AddWidgetPropertyData implements ChangeData{

	private List<WidgetProperty> properties;

	public List<WidgetProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<WidgetProperty> properties) {
		this.properties = properties;
	}
	
}
