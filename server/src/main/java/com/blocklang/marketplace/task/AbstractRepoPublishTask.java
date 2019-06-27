package com.blocklang.marketplace.task;

import java.util.Optional;

public abstract class AbstractRepoPublishTask {
	protected MarketplacePublishContext context;
	protected TaskLogger logger;
	
	public AbstractRepoPublishTask(MarketplacePublishContext context) {
		this.context = context;
		this.logger = new TaskLogger(context.getRepoPublishLogFile());
	}
	
	public abstract Optional<?> run();
}
