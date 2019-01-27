package com.blocklang.release.task;

public abstract class AbstractTask {
	protected AppBuildContext appBuildContext;
	
	public AbstractTask(AppBuildContext appBuildContext) {
		this.appBuildContext = appBuildContext;
	}
	
	public abstract boolean run();
}
