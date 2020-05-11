package com.blocklang.marketplace.task;

import com.blocklang.core.runner.AbstractTask;
import com.blocklang.core.runner.CliContext;

public abstract class AbstractPublishRepoTask extends AbstractTask<MarketplacePublishData>{

	public AbstractPublishRepoTask(CliContext<MarketplacePublishData> context) {
		super(context);
	}

}
