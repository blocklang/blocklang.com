package com.blocklang.marketplace.task;

import com.blocklang.core.runner.common.AbstractTask;
import com.blocklang.core.runner.common.CliContext;

public abstract class AbstractPublishRepoTask extends AbstractTask<MarketplacePublishData>{

	public AbstractPublishRepoTask(CliContext<MarketplacePublishData> context) {
		super(context);
	}

}
