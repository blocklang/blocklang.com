package com.blocklang.release.task;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
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
			processBuilder.redirectOutput(Redirect.appendTo(appBuildContext.getLogFilePath().toFile()));
			
			Process process = processBuilder.start();
			
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