package com.blocklang.core.controller;

import javax.servlet.http.HttpServletRequest;

public abstract class RequestUtil {

	/**
	 * 判断是否是 fetch 请求
	 * 
	 * @param request
	 * @return 如果是 ajax 或 fetch 请求则返回 true，否则返回 false
	 */
	public static boolean isFetch(HttpServletRequest request) {
		String header = request.getHeader("X-Requested-With");
		return header != null && "FetchApi".equals(header);
	}

	
}
