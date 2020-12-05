package com.blocklang.develop.data;

import javax.validation.constraints.NotBlank;

import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.DeviceType;

public class NewPageParam {

	@NotBlank(message = "{NotBlank.pageKey}")
	private String key;
	private String name;
	private String description;
	private Integer parentId; // parentId
	private String appType;
	private String deviceType;
	
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
	public AppType getAppType() {
		return AppType.fromKey(appType);
	}
	public void setAppType(String appType) {
		this.appType = appType;
	}
	public DeviceType getDeviceType() {
		return DeviceType.fromKey(deviceType);
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	
}
