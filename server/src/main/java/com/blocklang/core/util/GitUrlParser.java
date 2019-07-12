package com.blocklang.core.util;

import java.util.Optional;

public class GitUrlParser {

	public static Optional<GitUrlSegment> parse(String gitUrl) {
		if(org.apache.commons.lang3.StringUtils.isBlank(gitUrl)) {
			return Optional.empty();
		}
		// 一个完整的 gitUrl 示例
		// https://github.com/blocklang/blocklang.com.git
		String url = gitUrl.toLowerCase();
		
		if(!url.startsWith("https://")) {
			return Optional.empty();
		}
		// 1. 去除开头的 https://
		url = url.substring("https://".length());
		
		if(!url.endsWith(".git")) {
			return Optional.empty();
		}
		// 2. 去除结尾的 .git
		url = url.substring(0, url.length() - ".git".length());
		String[] segments = url.split("/");
		if(segments.length != 3) {
			return Optional.empty();
		}
		return Optional.of(new GitUrlSegment(segments[0], segments[1], segments[2]));
	}
}
