package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;

public class GitUrlParserTest {
	
	@Test
	public void parse_git_url_isBlank() {
		Optional<GitUrlSegment> segmentOption = GitUrlParser.parse(null);
		assertThat(segmentOption).isEmpty();
		
		segmentOption = GitUrlParser.parse(" ");
		assertThat(segmentOption).isEmpty();
	}
	
	@Test
	public void parse_git_url_not_starts_with_https() {
		Optional<GitUrlSegment> segmentOption = GitUrlParser.parse("http://github.com/jack/app.git");
		assertThat(segmentOption).isEmpty();
	}
	
	@Test
	public void parse_git_url_not_ends_with_git() {
		Optional<GitUrlSegment> segmentOption = GitUrlParser.parse("https://github.com/jack/app");
		assertThat(segmentOption).isEmpty();
	}
	
	@Test
	public void parse_git_url_not_three_segment() {
		Optional<GitUrlSegment> segmentOption = GitUrlParser.parse("https://github.com/jack/xxx/app.git");
		assertThat(segmentOption).isEmpty();
		
		segmentOption = GitUrlParser.parse("https://github.com/app.git");
		assertThat(segmentOption).isEmpty();
	}

	@Test
	public void parse_success() {
		Optional<GitUrlSegment> segmentOption = GitUrlParser.parse("https://github.com/jack/app.git");
		
		assertThat(segmentOption).isPresent();
		
		GitUrlSegment segment = segmentOption.get();
		assertThat(segment.getWebsite()).isEqualTo("github.com");
		assertThat(segment.getOwner()).isEqualTo("jack");
		assertThat(segment.getRepoName()).isEqualTo("app");
	}
}
