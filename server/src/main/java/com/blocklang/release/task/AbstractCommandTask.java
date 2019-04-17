package com.blocklang.release.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommandTask extends AbstractTask{

	static final Logger logger = LoggerFactory.getLogger(AbstractCommandTask.class);
	
	public AbstractCommandTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}

	protected boolean runCommand(Path workingDirectory, List<String> commands) {
		ProcessBuilder processBuilder = new ProcessBuilder(commands).directory(workingDirectory.toFile());
		
		try {
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			try( BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
				outReader.lines().iterator().forEachRemaining(line -> {
					appBuildContext.raw(line);
				});
			}
			
			if(process.isAlive()) {
				process.waitFor();
			}
			return process.exitValue() == 0;
		} catch (IOException | InterruptedException e) {
			appBuildContext.error(e);
		}
		return false;
	}

}