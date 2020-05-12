package com.blocklang.core.runner.common;

import java.util.Optional;

public abstract class AbstractAction {

	protected CliLogger logger;
	protected ExecutionContext context;
	
	public AbstractAction(ExecutionContext context) {
		this.context = context;
		this.logger = context.getLogger();
	}
	
	public abstract Optional<?> run();
}
