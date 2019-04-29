package com.blocklang.develop.data;

import javax.validation.constraints.NotBlank;

public class NewGroupParam {
	@NotBlank(message = "{NotBlank.groupKey}")
	private String key;
	private String name;
	private String description;
	private Integer parentId;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
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
	public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
}
