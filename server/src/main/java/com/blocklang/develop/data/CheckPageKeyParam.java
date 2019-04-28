package com.blocklang.develop.data;

import javax.validation.constraints.NotBlank;

import com.blocklang.core.constant.Constant;

public class CheckPageKeyParam {

	@NotBlank(message = "{NotBlank.pageKey}")
	private String key;
	private Integer groupId; // parentId

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Integer getGroupId() {
		if(groupId == null) {
			return Constant.TREE_ROOT_ID;
		}
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

}
