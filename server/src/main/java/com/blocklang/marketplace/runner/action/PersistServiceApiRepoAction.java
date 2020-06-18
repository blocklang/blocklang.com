package com.blocklang.marketplace.runner.action;

import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.util.SpringUtils;
import com.blocklang.marketplace.service.PersistApiRepoService;
import com.blocklang.marketplace.service.PersistServiceApiRepoService;

public class PersistServiceApiRepoAction extends AbstractPersistApiRepoAction {

	public PersistServiceApiRepoAction(ExecutionContext context) {
		super(context);
	}

	@Override
	protected PersistApiRepoService getPersistApiRepoService() {
		return SpringUtils.getBean(PersistServiceApiRepoService.class);
	}

}
