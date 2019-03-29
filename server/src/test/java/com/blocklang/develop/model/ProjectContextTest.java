package com.blocklang.develop.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

public class ProjectContextTest {
	
	@Test
	public void get_git_repository_directory() {
		ProjectContext context = new ProjectContext("jack", "app", "c:/blocklang");
		
		assertThat(context.getGitRepositoryDirectory().compareTo(Paths.get("c:/blocklang/gitRepo/jack/app"))).isEqualTo(0);
	}
	
}
