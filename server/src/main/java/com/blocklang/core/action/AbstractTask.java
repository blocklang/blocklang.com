package com.blocklang.core.action;

import java.util.Optional;

public abstract class AbstractTask {
	protected CliContext context;
	protected CliLogger logger;
	
	public AbstractTask(CliContext context) {
		this.context = context;
		this.logger = context.getLogger();
	}
	
	public abstract Optional<?> run();
}
