package com.blocklang.release.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import liquibase.util.SystemUtils;

public class InstallTask {

	private static final Logger logger = LoggerFactory.getLogger(InstallTask.class);
	
	private String projectName;
	private String version;
	private String mavenRootPath;
	private String projectsRootPath;
	
	public static void main(String[] args){
		// 设置工作目录
		String projectsRootPath = "E:\\data\\temp\\blocklang-release";
		String mavenRootPath = "C:\\Users\\Administrator\\.m2\\repository";
		String projectName = "demo_project";
		String version = "0.0.1-SNAPSHOT";
		
		InstallTask installTask = new InstallTask(projectsRootPath, mavenRootPath, projectName, version);
		installTask.run();
		
		String jarFilePath = installTask.getJarFilePath();
		System.out.println(Paths.get(jarFilePath).toFile().exists());
	}
	
	public InstallTask(String projectsRootPath, String mavenRootPath, String projectName, String version) {
		this.projectsRootPath = projectsRootPath;
		this.mavenRootPath = mavenRootPath;
		this.projectName = projectName;
		this.version = version;
	}
	
	public boolean run() {
		Assert.hasLength(this.projectsRootPath, "存放项目的根路径不能为空");
		Assert.hasLength(this.mavenRootPath, "maven 仓库的根路径不能为空");
		Assert.hasLength(this.projectName, "项目名不能为空");
		Assert.hasLength(this.version, "项目版本号不能为空");
		
		String projectRootPath = this.getProjectRootPath();
		List<String> commands = new ArrayList<>();
		if(SystemUtils.IS_OS_WINDOWS) {
			commands.add("mvnw.cmd");
		} else {
			commands.add("mvnw");
		}
		commands.add("clean");
		commands.add("install");
		
		ProcessBuilder processBuilder = new ProcessBuilder(commands).directory(Paths.get(projectRootPath).toFile());
		try {
			logger.info("开始执行 mvnw clean install 命令");
			Process process = processBuilder.start();
			
			String logFileDir = this.getLogDir();
			String logFileName = this.getLogFileName();
			Files.createDirectories(Paths.get(logFileDir));
			Files.copy(process.getInputStream(), Paths.get(logFileDir, logFileName));
			
			if(process.isAlive()) {
				process.waitFor();
			}
			// 经测试，在 windows 环境下，如果执行失败，则值为 1；如果执行成功，则值为0.
			int exitValue = process.exitValue();
			if (exitValue == 0) {
				logger.info("mvnw clean install 命令执行成功！");
				return true;
			} else {
				logger.error("mvnw clean install 命令执行失败！");
				return false;
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	public String getJarFilePath() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.mavenRootPath)
		  .append(File.separator)
		  .append("com")
		  .append(File.separator)
		  .append("blocklang")
		  .append(File.separator)
		  .append(this.projectName)
		  .append(File.separator)
		  .append(this.version)
		  .append(File.separator)
		  .append(this.projectName)
		  .append("-")
		  .append(this.version)
		  .append(".jar");
		return sb.toString();
	}
	
	private String getLogFileName() {
		return this.projectName + "-" + this.version + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + ".log";
	}

	private String getLogDir() {
		return this.getProjectRootPath() + File.separator + "logs";
	}

	private String getProjectRootPath() {
		StringBuilder sb = new StringBuilder(this.projectsRootPath);
		sb.append(File.separator)
		  .append("projects")
		  .append(File.separator)
		  .append(this.projectName);
		
		return sb.toString();
	}
	
}
