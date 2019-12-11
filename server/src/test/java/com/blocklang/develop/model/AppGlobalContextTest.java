package com.blocklang.develop.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class AppGlobalContextTest {
	
	@Test
	public void get_apps_directory() {
		AppGlobalContext context = new AppGlobalContext("c:/blocklang");
		assertThat(context.getAppsDirectory().compareTo(Paths.get("c:/blocklang/apps"))).isEqualTo(0);
	}
	
	@Test
	public void get_maven_repository_root_directory() {
		AppGlobalContext context = new AppGlobalContext("c:/blocklang", "c:/home/.m2");
		assertThat(context.getMavenRepositoryRootDirectory().compareTo(Paths.get("c:/home/.m2/repository"))).isEqualTo(0);
	}

}
