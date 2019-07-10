package com.blocklang.core.util;

public class GitUrlParser {

	public static GitUrlSegment parse(String gitUrl) {
		// 一个完整的 gitUrl 示例
		// https://github.com/blocklang/blocklang.com.git
		String url = gitUrl.toLowerCase();
		// 1. 去除开头的 https://
		if(url.startsWith("https://")) {
			url = url.substring("https://".length());
		}
		// 2. 去除结尾的 .git
		if(url.endsWith(".git")) {
			url = url.substring(0, url.length() - ".git".length());
		}
		String[] segments = url.split("/");
		return new GitUrlSegment(segments[0], segments[1], segments[2]);
	}
}
