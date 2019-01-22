package com.blocklang.release.task;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

public class MavenInstallTaskTest {

	@Test(expected = IllegalArgumentException.class)
	public void run_argument_is_empty() {
		MavenInstallTask task = new MavenInstallTask(null, null, null, null);
		task.run();
	}
	
	@Test
	public void get_jar_file_path() {
		MavenInstallTask task = new MavenInstallTask("projectsRootPath", "mavenRootPath", "app", "0.0.1");
		String expect = "mavenRootPath" 
				+ File.separator 
				+ "com" 
				+ File.separator 
				+ "blocklang" 
				+ File.separator 
				+ "app"
				+ File.separator 
				+ "0.0.1" 
				+ File.separator 
				+ "app-0.0.1.jar";
		assertThat(task.getJarFilePath(), equalTo(expect));
	}
	
}
