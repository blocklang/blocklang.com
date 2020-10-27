package com.blocklang.develop.designer.data;

import com.blocklang.develop.constant.RepositoryResourceType;

public class PageInfo {
	private Integer id;
	private String key;
	private String label;
	private String resourceType = RepositoryResourceType.PAGE.getKey(); // 资源类型，此类默认为页面

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

}
