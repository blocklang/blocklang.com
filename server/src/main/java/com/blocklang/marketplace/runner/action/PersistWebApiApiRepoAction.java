package com.blocklang.marketplace.runner.action;

import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.util.SpringUtils;
import com.blocklang.marketplace.service.PersistApiRepoService;
import com.blocklang.marketplace.service.PersistJsObjectApiRepoService;

public class PersistWebApiApiRepoAction extends AbstractPersistApiRepoAction {

	public PersistWebApiApiRepoAction(ExecutionContext context) {
		super(context);
	}

	@Override
	protected PersistApiRepoService getPersistApiRepoService() {
		return SpringUtils.getBean(PersistJsObjectApiRepoService.class);
	}

}
