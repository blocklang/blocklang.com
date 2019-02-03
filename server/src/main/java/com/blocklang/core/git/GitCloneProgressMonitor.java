package com.blocklang.core.git;

import org.eclipse.jgit.lib.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitCloneProgressMonitor implements ProgressMonitor {

	private static final Logger logger = LoggerFactory.getLogger(GitCloneProgressMonitor.class);
	
	@Override
	public void beginTask(String title, int totalWork) {
		logger.info("Begin processing a single task {}, {}", title, totalWork);
	}

	@Override
	public void endTask() {
		logger.info("Finish the current task");

	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void start(int totalTasks) {
		logger.info("start the task {}", totalTasks);
	}

	@Override
	public void update(int completed) {
		logger.info("Denote that {} work units have been completed", completed);
	}

}
