package com.blocklang.release.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.blocklang.core.git.GitUtils;

public class GitTagTaskTest {

	private String gitRepoDirectory = "gitRepo";
	
	@Test
	public void tag_success(@TempDir Path tempDir) throws IOException {
		Path folder = Files.createDirectory(tempDir.resolve(gitRepoDirectory));
		AppBuildContext context = new AppBuildContext(
				folder.toString(), 
				"c:/Users/Administrator/.m2", 
				null, 
				"jack", 
				"app", 
				"0.0.1", 
				"description",
				"jdk_version");
		
		GitUtils.init(context.getGitRepositoryDirectory(), "jack", "a@a.com");
		
		GitTagTask task = new GitTagTask(context);
		assertThat(task.run()).isPresent();
	}
}
