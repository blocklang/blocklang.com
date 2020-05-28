package com.blocklang.marketplace.apiparser;

public class ApiRepoPathInfo {

	private String order;
	private String description;

	public ApiRepoPathInfo(String order, String description) {
		super();
		this.order = order;
		this.description = description;
	}

	public String getOrder() {
		return order;
	}

	public String getDescription() {
		return description;
	}

}
