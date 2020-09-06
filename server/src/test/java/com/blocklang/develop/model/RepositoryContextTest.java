package com.blocklang.develop.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class RepositoryContextTest {
	
	@Test
	public void getGitRepositoryDirectory() {
		RepositoryContext context = new RepositoryContext("jack", "app", "c:/blocklang");
		
		assertThat(context.getGitRepositoryDirectory().compareTo(Paths.get("c:/blocklang/gitRepo/jack/app"))).isEqualTo(0);
	}
	
}
