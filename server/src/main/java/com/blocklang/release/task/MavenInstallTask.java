package com.blocklang.release.task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MavenInstallTask extends AbstractTask{

	private static final Logger logger = LoggerFactory.getLogger(MavenInstallTask.class);
	
	public static void main(String[] args){
		// 设置工作目录
		String projectsRootPath = "E:\\data\\temp\\blocklang-release";
		String mavenRootPath = "C:\\Users\\Administrator\\.m2";
		String projectName = "demo_app";
		String version = "0.0.1-SNAPSHOT";
		
		AppBuildContext appBuildContext = new AppBuildContext(projectsRootPath, mavenRootPath, projectName, version);
		
		MavenInstallTask installTask = new MavenInstallTask(appBuildContext);
		installTask.run();
		// String jarFilePath = installTask.getJarFilePath();
		// TODO: 只有 pom.xml 中的内容调整后，才返回 true
		// System.out.println(Paths.get(jarFilePath).toFile().exists());
	}
	
	public MavenInstallTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}
	
	/**
	 * 执行 <code>./mvnw clean install</code> 命令
	 * 
	 * @return
	 */
	public boolean run() {
		List<String> commands = new ArrayList<>();
		if(SystemUtils.IS_OS_WINDOWS) {
			commands.add("mvnw.cmd");
		} else {
			commands.add("mvnw");
		}
		commands.add("clean");
		commands.add("install");
		
		logger.info("开始执行 mvnw clean install 命令");
		
		Path workingDirectory = appBuildContext.getServerProjectRootDirectory();
		boolean success = runCommand(workingDirectory, commands);
		if(success) {
			logger.info("mvnw clean install 命令执行成功！");
		} else {
			logger.error("mvnw clean install 命令执行失败！");
		}
		return success;
	}
}
