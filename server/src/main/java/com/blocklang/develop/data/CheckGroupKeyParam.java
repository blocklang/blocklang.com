package com.blocklang.develop.data;

import javax.validation.constraints.NotBlank;

public class CheckGroupKeyParam {
	@NotBlank(message = "{NotBlank.groupKey}")
	private String key;
	private Integer parentId;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	
}
