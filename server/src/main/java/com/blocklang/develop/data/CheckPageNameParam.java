package com.blocklang.develop.data;

import com.blocklang.develop.constant.AppType;

public class CheckPageNameParam {

	private String name;
	private Integer groupId; // parentId
	private String appType;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
