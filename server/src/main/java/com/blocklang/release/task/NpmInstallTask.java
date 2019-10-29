package com.blocklang.release.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
	public Optional<Boolean> run() {
		List<String> commands = new ArrayList<>();
		if(SystemUtils.IS_OS_WINDOWS) {
			commands.add("npm.cmd");
		}else {
			commands.add("npm");
		}
		commands.add("install");
		
		boolean result = runCommand(appBuildContext.getClientProjectRootDirectory(), commands);
		if(result) {
			return Optional.of(true);
		} else {
			return Optional.empty();
		}
	}
}
