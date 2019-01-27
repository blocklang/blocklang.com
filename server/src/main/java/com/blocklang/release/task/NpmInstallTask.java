package com.blocklang.release.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpmInstallTask extends AbstractCommandTask {

	static final Logger logger = LoggerFactory.getLogger(NpmInstallTask.class);
	
	public static void main(String[] args) {
		String projectsRootPath = "E:\\data\\temp\\blocklang-release";
		String mavenRootPath = "C:\\Users\\Administrator\\.m2";
		String projectName = "demo_app";
		String version = "0.0.1-SNAPSHOT";
		
		AppBuildContext appBuildContext = new AppBuildContext(projectsRootPath, mavenRootPath, projectName, version);
		
		NpmInstallTask task = new NpmInstallTask(appBuildContext);
		task.run();
	}
	
	public NpmInstallTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}
	
	@Override
	public boolean run() {
		List<String> commands = new ArrayList<>();
		if(SystemUtils.IS_OS_WINDOWS) {
			commands.add("cnpm.cmd");
		}else {
			commands.add("cnpm");
		}
		commands.add("install");
		
		logger.info("开始执行 cnpm install 命令");
		boolean success = runCommand(appBuildContext.getClientProjectRootDirectory(), commands);
		if(success) {
			logger.info("cnpm install 命令执行成功！");
		} else {
			logger.error("cnpm install 命令执行失败！");
		}

		return success;
	}
}
