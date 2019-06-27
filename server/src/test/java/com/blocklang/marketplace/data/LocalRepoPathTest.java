package com.blocklang.marketplace.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import com.blocklang.marketplace.data.LocalRepoPath;

public class LocalRepoPathTest {

	private LocalRepoPath localRepoPathInfo;
	
	@Before
	public void setUp() {
		localRepoPathInfo = new LocalRepoPath(
				"c:/blocklang",
				"https://github.com/jack/app.git");
	}
	
	@Test
	public void get_repo_root_directory() {
		assertThat(localRepoPathInfo.getRepoRootDirectory()
				.compareTo(Paths.get("c:/blocklang/marketplace/github.com/jack/app"))).isEqualTo(0);
	}
	
	@Test
	public void get_repo_source_directory() {
		assertThat(localRepoPathInfo.getRepoSourceDirectory()
				.compareTo(Paths.get("c:/blocklang/marketplace/github.com/jack/app/source"))).isEqualTo(0);
	}
	
	@Test
	public void parse_git_url_success() {
		assertThat(localRepoPathInfo.getGitUrl()).isEqualTo("https://github.com/jack/app.git");
		assertThat(localRepoPathInfo.getWebsite()).isEqualTo("github.com");
		assertThat(localRepoPathInfo.getOwner()).isEqualTo("jack");
		assertThat(localRepoPathInfo.getRepoName()).isEqualTo("app");
	}
	
}
