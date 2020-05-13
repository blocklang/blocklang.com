package com.blocklang.core.runner.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.util.Assert;

public class CliCommand {

	private CliLogger logger;
	public CliCommand(CliLogger logger) {
		this.logger = logger;
	}
	
	public boolean run(Path workingDirectory, String... commands) {
		Assert.isTrue(commands.length > 1, "至少要包含一个命令");
		// 兼容 windows 和 linux
		// 处理第一个命令，在 windows 中增加 .cmd 后缀
		commands[0] = getCommandName(commands[0]);
		
		ProcessBuilder processBuilder = new ProcessBuilder(commands).directory(workingDirectory.toFile());
		
		try {
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			try( BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
				outReader.lines().iterator().forEachRemaining(line -> {
					logger.log(line);
				});
			}
			
			if(process.isAlive()) {
				process.waitFor();
			}
			return process.exitValue() == 0;
		} catch (IOException | InterruptedException e) {
			logger.error(e);
		}
		return false;
	}
	
	// FIXME: 重命名，此名字意图不够明确
	private String getCommandName(String command) {
		return SystemUtils.IS_OS_WINDOWS ? command + ".cmd" : command;
	}
	
}
