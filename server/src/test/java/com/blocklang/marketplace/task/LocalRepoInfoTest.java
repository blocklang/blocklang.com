package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class LocalRepoInfoTest {

	private LocalRepoInfo localRepoInfo;
	
	@Before
	public void setUp() {
		localRepoInfo = new LocalRepoInfo(
				"c:/blocklang",
				"https://github.com/jack/app.git");
	}
	
	@Test
	public void get_repo_root_directory() {
		assertThat(localRepoInfo.getRepoRootDirectory()
				.compareTo(Paths.get("c:/blocklang/marketplace/github.com/jack/app"))).isEqualTo(0);
	}
	
	@Test
	public void get_repo_source_directory() {
		assertThat(localRepoInfo.getRepoSourceDirectory()
				.compareTo(Paths.get("c:/blocklang/marketplace/github.com/jack/app/source"))).isEqualTo(0);
	}
	
	@Test
	public void parse_git_url_success() {
		assertThat(localRepoInfo.getGitUrl()).isEqualTo("https://github.com/jack/app.git");
		assertThat(localRepoInfo.getWebsite()).isEqualTo("github.com");
		assertThat(localRepoInfo.getOwner()).isEqualTo("jack");
		assertThat(localRepoInfo.getRepoName()).isEqualTo("app");
	}
	
}
