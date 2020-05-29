package com.blocklang.marketplace.data.changelog;

import java.util.List;

/**
 * 
 * @deprecated 
 *
 */
public class AddWidgetProperty implements ChangeData{

	private List<WidgetProperty> properties;

	public List<WidgetProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<WidgetProperty> properties) {
		this.properties = properties;
	}
	
}
