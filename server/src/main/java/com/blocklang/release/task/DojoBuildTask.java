package com.blocklang.release.task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;

public class DojoBuildTask extends AbstractCommandTask{

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
	@Override
	public Optional<Boolean> run() {
		List<String> commands = new ArrayList<>();

		if(SystemUtils.IS_OS_WINDOWS) {
			commands.add("dojo.cmd");
		} else {
			commands.add("dojo");
		}
		commands.add("build");
		commands.add("--mode");
		commands.add("dist");
		
		Path workingDirectory = appBuildContext.getClientProjectRootDirectory();
		boolean success = runCommand(workingDirectory, commands);
		if(success) {
			return Optional.of(success);
		} else {
			return Optional.empty();
		}
	}
	
}
