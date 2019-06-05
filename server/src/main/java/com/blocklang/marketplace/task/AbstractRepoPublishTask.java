package com.blocklang.marketplace.task;

import java.util.Optional;

public abstract class AbstractRepoPublishTask {
	protected MarketplacePublishContext marketplacePublishContext;
	protected TaskLogger logger;
	
	public AbstractRepoPublishTask(MarketplacePublishContext marketplacePublishContext) {
		this.marketplacePublishContext = marketplacePublishContext;
		this.logger = new TaskLogger(marketplacePublishContext.getRepoPublishLogFile());
	}
	
	public abstract Optional<?> run();
}
