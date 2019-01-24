package com.blocklang.release.task;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class AppBuildContextTest {

	private AppBuildContext context;
	
	@Before
	public void setUp() {
		context = new AppBuildContext("c:\\blocklang", "c:\\Users\\Administrator\\.m2", "app", "0.0.1");
	}
	
	@Test
	public void get_client_project_root_directory() {
		assertThat(context.getClientProjectRootDirectory().compareTo(Paths.get("c:\\blocklang\\projects\\app\\client")), is(0));
	}
	
	@Test
	public void get_server_project_root_directory() {
		assertThat(context.getServerProjectRootDirectory().compareTo(Paths.get("c:\\blocklang\\projects\\app\\server")), is(0));
	}
	
	@Test
	public void get_maven_install_jar_path() {
		assertThat(
				context.getMavenInstallJar().compareTo(Paths.get("c:\\Users\\Administrator\\.m2\\repository\\com\\blocklang\\app\\0.0.1\\app-0.0.1.jar")),
				is(0));
	}
	
	@Test
	public void get_log_directory() {
		assertThat(context.getLogDirectory().compareTo(Paths.get("c:\\blocklang\\projects\\app\\logs")), is(0));
	}
	
	@Test
	public void get_log_file() {
		String logFileName = context.getLogFileName();
		assertThat(logFileName, startsWith("app-0.0.1-"));
		assertThat(logFileName, endsWith(".log"));
	}
	
	// 在同一个上下文中，多次调用获取的日志文件名要相同
	@Test
	public void get_log_file_call_twice() throws InterruptedException {
		String first = context.getLogFileName();
		TimeUnit.SECONDS.sleep(1);
		String second = context.getLogFileName();
		
		assertThat(first, equalTo(second));
	}
	
	@Test
	public void get_dojo_dist_directory() {
		assertThat(context.getDojoDistDirectory().compareTo(Paths.get("c:\\blocklang\\projects\\app\\client\\output\\dist")), is(0));
	}
	
	@Test
	public void get_spring_boot_templates_directory() {
		assertThat(context.getSpringBootTemplatesDirectory().compareTo(Paths.get("c:\\blocklang\\projects\\app\\server\\src\\main\\resources\\templates")), is(0));
	}
	
	@Test
	public void get_spring_boot_static_directory() {
		assertThat(context.getSpringBootStaticDirectory().compareTo(Paths.get("c:\\blocklang\\projects\\app\\server\\src\\main\\resources\\static")), is(0));
	}
	
	@Test
	public void get_index_file_name() {
		assertThat(context.getIndexFileName(), equalTo("index.html"));
	}
}
