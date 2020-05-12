package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class GitUrlSegmentTest {
	
	@Test
	public void of_git_remote_url_isBlank() {
		GitUrlSegment segment = GitUrlSegment.of(null);
		assertThat(segment).isNull();
		
		segment = GitUrlSegment.of(" ");
		assertThat(segment).isNull();
	}
	
	@Test
	public void of_git_remote_url_not_starts_with_https() {
		GitUrlSegment segment = GitUrlSegment.of("http://github.com/jack/app.git");
		assertThat(segment).isNull();
	}
	
	@Test
	public void of_git_remote_url_not_ends_with_git() {
		GitUrlSegment segment = GitUrlSegment.of("https://github.com/jack/app");
		assertThat(segment).isNull();;
	}
	
	@Test
	public void of_git_url_not_three_segment() {
		// 2 segments
		GitUrlSegment segment = GitUrlSegment.of("https://github.com/app.git");
		assertThat(segment).isNull();
		
		// 4 segments
		segment = GitUrlSegment.of("https://github.com/jack/xxx/app.git");
		assertThat(segment).isNull();
	}

	@Test
	public void of_success() {
		GitUrlSegment segment = GitUrlSegment.of("https://github.com/jack/app.git");
		
		assertThat(segment).isNotNull();
		
		assertThat(segment.getWebsite()).isEqualTo("github.com");
		assertThat(segment.getOwner()).isEqualTo("jack");
		assertThat(segment.getRepoName()).isEqualTo("app");
	}
}
