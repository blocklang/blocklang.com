package com.blocklang.release.task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DojoBuildTask extends AbstractTask{

	private static final Logger logger = LoggerFactory.getLogger(DojoBuildTask.class);
	
	public static void main(String[] args) {
		String projectsRootPath = "E:\\data\\temp\\blocklang-release";
		String mavenRootPath = "C:\\Users\\Administrator\\.m2";
		String projectName = "demo_app";
		String version = "0.0.1-SNAPSHOT";
		
		AppBuildContext appBuildContext = new AppBuildContext(projectsRootPath, mavenRootPath, projectName, version);
		
		DojoBuildTask task = new DojoBuildTask(appBuildContext);
		task.run();
	}
	
	public DojoBuildTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}
	
	/**
	  * 执行 <code>dojo build --mode dist</code> 命令
	 * 
	 * @return
	 */
	public boolean run() {
		List<String> commands = new ArrayList<>();

		if(SystemUtils.IS_OS_WINDOWS) {
			commands.add("dojo.cmd");
		} else {
			commands.add("dojo");
		}
		commands.add("build");
		commands.add("--mode");
		commands.add("dist");
		
		logger.info("开始执行 dojo build --mode dist 命令");
		
		Path workingDirectory = appBuildContext.getClientProjectRootDirectory();
		boolean success = runCommand(workingDirectory, commands);
		if(success) {
			logger.info("dojo build --mode dist 命令执行成功！");
		} else {
			logger.error("dojo build --mode dist 命令执行失败！");
		}
		return success;
	}
	
}
