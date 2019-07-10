package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class GitUrlParserTest {

	@Test
	public void parse() {
		GitUrlSegment segment = GitUrlParser.parse("https://github.com/jack/app.git");
		assertThat(segment.getWebsite()).isEqualTo("github.com");
		assertThat(segment.getOwner()).isEqualTo("jack");
		assertThat(segment.getRepoName()).isEqualTo("app");
	}
}
