package com.blocklang.marketplace.apirepo;

public enum ParseResult {

	/**
	 * 执行了解析，并且解析成功
	 */
	SUCCESS,
	/**
	 * 执行了解析，但是解析失败
	 */
	FAILED,
	/**
	 * 已成功解析过，因此跳过本次解析
	 */
	ABORT;
	
}
