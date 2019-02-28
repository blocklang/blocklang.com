package com.blocklang.core.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

public abstract class SpringMvcUtil {
	
	/**
	 * 获取从指定索引开始的剩余 url
	 * 
	 * @param req
	 * @param startIndex
	 * @return url
	 */
	public static String getRestUrl(HttpServletRequest req, int startIndex) {
		String restOfTheUrl = (String) req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String[] segments = StringUtils.split(restOfTheUrl, "/");
		return StringUtils.join(segments, "/", startIndex, segments.length);
	}
}
