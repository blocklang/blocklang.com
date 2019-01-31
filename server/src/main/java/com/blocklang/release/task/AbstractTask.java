package com.blocklang.release.task;

import java.io.IOException;
import java.util.Optional;

public abstract class AbstractTask {
	
	protected AppBuildContext appBuildContext;
	
	public AbstractTask(AppBuildContext appBuildContext) {
		this.appBuildContext = appBuildContext;
	}
	
	public abstract Optional<?> run() throws IOException;
}
