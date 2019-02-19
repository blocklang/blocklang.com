package com.blocklang.release.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class AppBuildContextTest {

	private AppBuildContext context;
	
	@Before
	public void setUp() {
		context = new AppBuildContext("c:/blocklang", "c:/Users/Administrator/.m2", null, "jack", "app", "0.0.1");
	}
	
	@Test
	public void get_client_project_root_directory() {
		assertThat(context.getClientProjectRootDirectory().compareTo(Paths.get("c:/blocklang/projects/app/client"))).isEqualTo(0);
	}
	
	@Test
	public void get_server_project_root_directory() {
		assertThat(context.getServerProjectRootDirectory().compareTo(Paths.get("c:/blocklang/projects/app/server"))).isEqualTo(0);
	}
	
	@Test
	public void get_maven_install_jar_path() {
		assertThat(context.getMavenInstallJar().compareTo(Paths.get("c:/Users/Administrator/.m2/repository/com/blocklang/app/0.0.1/app-0.0.1.jar"))).isEqualTo(0);
	}
	
	@Test
	public void get_log_file_path() throws IOException {
		Path logFilePath = context.getLogFilePath();
		assertThat(logFilePath.getParent().compareTo(Paths.get("c:/blocklang/projects/app/logs"))).isEqualTo(0);
		
		String logFileName = logFilePath.getFileName().toString();
		
		assertThat(logFileName).startsWith("app-0.0.1-");
		assertThat(logFileName).endsWith(".log");
	}
	
	// 在同一个上下文中，多次调用获取的日志文件名要相同
	@Test
	public void get_log_file_path_call_twice() throws InterruptedException, IOException {
		Path first = context.getLogFilePath();
		TimeUnit.SECONDS.sleep(1);
		Path second = context.getLogFilePath();
		
		assertThat(first).isEqualTo(second);
	}
	
	@Test
	public void get_dojo_dist_directory() {
		assertThat(context.getDojoDistDirectory().compareTo(Paths.get("c:/blocklang/projects/app/client/output/dist"))).isEqualTo(0);
	}
	
	@Test
	public void get_spring_boot_templates_directory() {
		assertThat(context.getSpringBootTemplatesDirectory().compareTo(Paths.get("c:/blocklang/projects/app/server/src/main/resources/templates"))).isEqualTo(0);
	}
	
	@Test
	public void get_spring_boot_static_directory() {
		assertThat(context.getSpringBootStaticDirectory().compareTo(Paths.get("c:/blocklang/projects/app/server/src/main/resources/static"))).isEqualTo(0);
	}
	
	@Test
	public void get_index_file_name() {
		assertThat(context.getIndexFileName()).isEqualTo("index.html");
	}
	
	@Test
	public void get_git_repository_directory() {
		assertThat(context.getGitRepositoryDirectory().compareTo(Paths.get("c:/blocklang/gitRepo/jack/app"))).isEqualTo(0);
	}
	
	@Test
	public void get_tag_name() {
		assertThat(context.getTagName()).isEqualTo("v0.0.1");
	}
	
	@Test
	public void get_project_template_client_directory() {
		assertThat(context.getProjectTemplateClientDirectory().compareTo(Paths.get("c:/blocklang/template/client"))).isEqualTo(0);
	}

	@Test
	public void get_project_template_server_directory() {
		assertThat(context.getProjectTemplateServerDirectory().compareTo(Paths.get("c:/blocklang/template/server"))).isEqualTo(0);
	}
}
