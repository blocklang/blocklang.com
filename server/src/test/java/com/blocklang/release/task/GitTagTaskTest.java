package com.blocklang.release.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.blocklang.core.git.GitUtils;

public class GitTagTaskTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private String gitRepoDirectory = "gitRepo";
	
	@Test
	public void tag_success() throws IOException {
		File folder = tempFolder.newFolder(gitRepoDirectory);
		AppBuildContext context = new AppBuildContext(
				folder.getPath(), 
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
