package com.blocklang.marketplace.apirepo;

/**
 * 目录名由两部分组成，如 `202005151827_button`，前半部分是时间戳，是不允许改变的，后半部分是 widget 名，是可以重命名的
 * 
 * @author Zhengwei Jin
 *
 */
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
