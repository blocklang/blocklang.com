package com.blocklang.core.runner.common;

import java.util.Optional;

public abstract class AbstractTask<T> {
	protected T data;
	protected CliLogger logger;
	protected CliContext<T> context;
	
	public AbstractTask(CliContext<T> context) {
		this.context = context;
		this.data = context.getData();
		this.logger = context.getLogger();
	}
	
	public abstract Optional<?> run();
}
