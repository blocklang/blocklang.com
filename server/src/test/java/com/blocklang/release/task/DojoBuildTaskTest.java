package com.blocklang.release.task;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

public class DojoBuildTaskTest {

	@Test(expected = IllegalArgumentException.class)
	public void run_argument_is_empty() {
		DojoBuildTask task = new DojoBuildTask(null, null, null);
		task.run();
	}
	
	@Test
	public void get_dis_directory() {
		DojoBuildTask task = new DojoBuildTask("projectsRootPath", "app", "0.0.1");
		String expect = "projectsRootPath" 
				+ File.separator 
				+ "projects"
				+ File.separator 
				+ "app" 
				+ File.separator 
				+ "client" 
				+ File.separator 
				+ "output"
				+ File.separator 
				+ "dist";
		assertThat(task.getDistDirectory(), equalTo(expect));
	}
}
