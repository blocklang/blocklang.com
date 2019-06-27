package com.blocklang.marketplace.data;

public class ApiJson {
	private String name;
	private String displayName;
	private String version;
	private String description;
	private String category;
	private String[] components;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String[] getComponents() {
		if(components == null) {
			return new String[] {};
		}
		return components;
	}

	public void setComponents(String[] components) {
		this.components = components;
	}

}
