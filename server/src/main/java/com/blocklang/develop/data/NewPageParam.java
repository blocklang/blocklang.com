package com.blocklang.develop.data;

import javax.validation.constraints.NotBlank;

import com.blocklang.develop.constant.AppType;

public class NewPageParam {

	@NotBlank(message = "{NotBlank.pageKey}")
	private String key;
	private String name;
	private String description;
	private Integer groupId; // parentId
	private String appType;
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
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public AppType getAppType() {
		return AppType.fromKey(appType);
	}
	public void setAppType(String appType) {
		this.appType = appType;
	}
	
}
