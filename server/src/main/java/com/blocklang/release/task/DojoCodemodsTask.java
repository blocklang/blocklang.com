package com.blocklang.release.task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;

/**
 * 用于生成 Dojo app
 * 
 * @author jinzw
 *
 */
public class DojoCodemodsTask extends AbstractCommandTask{

	public DojoCodemodsTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}

	/**
	 * 执行 <code>codemods --library dojo --modelDir ../.blocklang_models</code> 命令
	 */
	@Override
	public Optional<?> run() {
		List<String> commands = new ArrayList<>();

		if(SystemUtils.IS_OS_WINDOWS) {
			commands.add("codemods.cmd");
		} else {
			commands.add("codemods");
		}
		commands.add("--library");
		commands.add("dojo");
		commands.add("--modelDir");
		commands.add("../.blocklang_models");
		
		Path workingDirectory = appBuildContext.getClientProjectRootDirectory();
		boolean success = runCommand(workingDirectory, commands);
		if(success) {
			return Optional.of(success);
		} else {
			return Optional.empty();
		}
	}

}
