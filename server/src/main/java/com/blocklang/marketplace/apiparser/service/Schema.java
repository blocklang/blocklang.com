package com.blocklang.marketplace.apiparser.service;

public class Schema {

	private String type;
	private String name;
	private String description;
	private Schema properties;

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

	public Schema getProperties() {
		return properties;
	}

	public void setProperties(Schema properties) {
		this.properties = properties;
	}

}
