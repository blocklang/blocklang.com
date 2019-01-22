package com.blocklang.release.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class DojoBuildTask {

	private static final Logger logger = LoggerFactory.getLogger(DojoBuildTask.class);
	
	private String projectName;
	private String version;
	private String projectsRootPath;
	
	public static void main(String[] args) {
		DojoBuildTask task = new DojoBuildTask("E:\\data\\temp\\blocklang-release", "demo_app", "0.0.1");
		task.run();
	}
	
	public DojoBuildTask(String projectsRootPath, String projectName, String version) {
		this.projectsRootPath = projectsRootPath;
		this.projectName = projectName;
		this.version = version;
	}
	
	/**
	 * 执行 <code>dojo build --mode dist</code> 命令
	 * 
	 * @return
	 */
	public boolean run() {
		Assert.hasLength(this.projectsRootPath, "存放项目的根路径不能为空");
		Assert.hasLength(this.projectName, "项目名不能为空");
		Assert.hasLength(this.version, "项目版本号不能为空");
		
		List<String> commands = new ArrayList<>();

		if(SystemUtils.IS_OS_WINDOWS) {
			commands.add("dojo.cmd");
		} else {
			commands.add("dojo");
		}
		commands.add("build");
		commands.add("--mode");
		commands.add("dist");
		
		String dojoProjectRootPath = this.getProjectRootPath();
		ProcessBuilder processBuilder = new ProcessBuilder(commands).directory(Paths.get(dojoProjectRootPath).toFile());
		logger.info("开始执行 dojo build --mode dist 命令");
		
		try {
			
			String logFileDir = this.getLogDirectory();
			String logFileName = this.getLogFileName();
			Files.createDirectories(Paths.get(logFileDir));
			processBuilder.redirectErrorStream(true);
			processBuilder.redirectOutput(Paths.get(logFileDir, logFileName).toFile());
			
			Process process = processBuilder.start();
			
			if(process.isAlive()) {
				process.waitFor();
			}
			int exitValue = process.exitValue();
			if (exitValue == 0) {
				logger.info("dojo build --mode dist 命令执行成功！");
				return true;
			} else {
				logger.error("dojo build --mode dist 命令执行失败！");
				return false;
			}
		} catch (IOException | InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		
		return false;
	}
	
	public String getDistDirectory() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getProjectRootPath())
		  .append(File.separator)
		  .append("output")
		  .append(File.separator)
		  .append("dist");

		return sb.toString();
	}

	private String getLogFileName() {
		return this.projectName + "-" + this.version + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + ".log";
	}

	private String getLogDirectory() {
		return this.getProjectRootPath() + File.separator + "logs";
	}
	
	private String getProjectRootPath() {
		StringBuilder sb = new StringBuilder(this.projectsRootPath);
		sb.append(File.separator)
		  .append("projects")
		  .append(File.separator)
		  .append(this.projectName)
		  .append(File.separator)
		  .append("client");
		
		return sb.toString();
	}
	
}
