package com.blocklang.marketplace.apirepo.service.data;

import java.util.ArrayList;
import java.util.List;

public class Schema {

	private String type;
	private String name;
	private String description;
	private List<Schema> properties = new ArrayList<Schema>();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Schema> getProperties() {
		return properties;
	}

	public void setProperties(List<Schema> properties) {
		this.properties = properties;
	}

}
