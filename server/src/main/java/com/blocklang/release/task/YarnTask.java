package com.blocklang.release.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YarnTask extends AbstractCommandTask {

	static final Logger logger = LoggerFactory.getLogger(YarnTask.class);
	
	public YarnTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}
	
	@Override
	public Optional<Boolean> run() {
		List<String> commands = new ArrayList<>();
		if(SystemUtils.IS_OS_WINDOWS) {
			commands.add("yarn.cmd");
		}else {
			commands.add("yarn");
		}
		
		boolean result = runCommand(appBuildContext.getClientProjectRootDirectory(), commands);
		if(result) {
			return Optional.of(true);
		} else {
			return Optional.empty();
		}
	}
}
