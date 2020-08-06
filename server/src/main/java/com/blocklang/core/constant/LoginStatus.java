package com.blocklang.core.constant;

public enum LoginStatus {
	
	NOT_LOGIN("NotLogin"),
	FAILED("Failed"),
	LOGINED("Logined"),
	NEED_COMPLETE_USER_INFO("NeedCompleteUserInfo");
	
	private final String status;

	private LoginStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
	
}
