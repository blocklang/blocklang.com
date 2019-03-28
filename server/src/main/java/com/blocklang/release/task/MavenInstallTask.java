package com.blocklang.release.task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;

public class MavenInstallTask extends AbstractCommandTask{

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
	@Override
	public Optional<Boolean> run() {
		List<String> commands = new ArrayList<>();
		Path workingDirectory = appBuildContext.getServerProjectRootDirectory();
		if(SystemUtils.IS_OS_WINDOWS) {
			commands.add("mvnw.cmd");
		} else {
			// 在 linux 系统下，为 mvnw 添加可执行权限
			workingDirectory.resolve("mvnw").toFile().setExecutable(true);
			commands.add("./mvnw");
		}
		commands.add("clean");
		commands.add("install");
		
		boolean success = runCommand(workingDirectory, commands);
		if(success) {
			return Optional.of(true);
		} else {
			return Optional.empty();
		}
	}
}
