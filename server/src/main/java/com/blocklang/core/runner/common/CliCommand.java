package com.blocklang.core.runner.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class CliCommand {

	private CliLogger logger;
	public CliCommand(CliLogger logger) {
		this.logger = logger;
	}
	
	public boolean run(Path workingDirectory, String... commands) {
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
	
}
