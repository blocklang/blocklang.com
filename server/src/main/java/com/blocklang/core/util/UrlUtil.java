package com.blocklang.core.util;

import org.apache.commons.lang3.StringUtils;

public abstract class UrlUtil {

	public static String trimHttpInUrl(String url) {
		if(StringUtils.isBlank(url)) {
			return "";
		}
		if(StringUtils.startsWithIgnoreCase(url, "http:")) {
			return url.substring("http:".length());
		}
		return url;
	}
	
}
