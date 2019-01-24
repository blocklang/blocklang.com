package com.blocklang.release.task;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.util.Assert;

public class AppBuildContext {

	private String projectName;
	private String version;
	private String mavenRootPath;
	private String projectsRootPath;
	
	private LocalDateTime startLogTime;
	
	public AppBuildContext(String projectsRootPath, 
			String mavenRootPath, 
			String projectName, 
			String version) {
		Assert.hasLength(projectsRootPath, "存放项目的根路径不能为空");
		Assert.hasLength(mavenRootPath, "maven 仓库的根路径不能为空");
		Assert.hasLength(projectName, "项目名不能为空");
		Assert.hasLength(version, "项目版本号不能为空");
		
		this.projectsRootPath = projectsRootPath;
		this.mavenRootPath = mavenRootPath;
		this.projectName = projectName;
		this.version = version;
	}

	private Path getProjectRootDirectory() {
		return Paths.get(this.projectsRootPath, "projects", this.projectName);
	}
	
	public Path getClientProjectRootDirectory() {
		return this.getProjectRootDirectory().resolve("client");
	}

	public Path getServerProjectRootDirectory() {
		return this.getProjectRootDirectory().resolve("server");
	}

	public Path getMavenInstallJar() {
		return Paths.get(this.mavenRootPath, 
				"repository", 
				"com", 
				"blocklang", 
				this.projectName, 
				this.version,
				this.projectName + "-" + this.version + ".jar");
	}

	public Path getLogDirectory() {
		return this.getProjectRootDirectory().resolve("logs");
	}
	
	public String getLogFileName() {
		if(startLogTime == null) {
			startLogTime = LocalDateTime.now();
		}
		return this.projectName + "-" + this.version + "-" + startLogTime.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + ".log";
	}

	public Path getDojoDistDirectory() {
		return this.getClientProjectRootDirectory().resolve("output").resolve("dist");
	}

	private Path getSpringBootMainResourcesDirectory() {
		return this.getServerProjectRootDirectory().resolve("src").resolve("main").resolve("resources");
	}
	
	public Path getSpringBootTemplatesDirectory() {
		return getSpringBootMainResourcesDirectory().resolve("templates");
	}
	
	public Path getSpringBootStaticDirectory() {
		return getSpringBootMainResourcesDirectory().resolve("static");
	}
	
	public String getIndexFileName() {
		return "index.html";
	}
}
