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

public class NpmInstallTask {

	private static final Logger logger = LoggerFactory.getLogger(NpmInstallTask.class);
	
	private String projectName;
	private String version;
	private String projectsRootPath;
	
	public static void main(String[] args) {
		NpmInstallTask task = new NpmInstallTask("E:\\data\\temp\\blocklang-release", "demo_app", "0.0.1");
		task.run();
	}
	
	public NpmInstallTask(String projectsRootPath, String projectName, String version) {
		this.projectsRootPath = projectsRootPath;
		this.projectName = projectName;
		this.version = version;
	}
	
	private String getCommandName() {
		return "cnpm";
	}
	
	public boolean run() {
		
		List<String> commands = new ArrayList<>();
		String commandName = this.getCommandName();
		if(SystemUtils.IS_OS_WINDOWS) {
			commands.add(commandName + ".cmd");
		}else {
			commands.add(commandName);
		}
		commands.add("install");
		
		ProcessBuilder processBuilder = new ProcessBuilder(commands).directory(Paths.get(this.getProjectRootPath()).toFile());
		logger.info("开始执行 cnpm install 命令");
		
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
				logger.info("cnpm install 命令执行成功！");
				return true;
			} else {
				logger.error("cnpm install 命令执行失败！");
				return false;
			}
		} catch (IOException | InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		
		return false;
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
