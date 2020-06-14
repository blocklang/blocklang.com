package com.blocklang.marketplace.runner.action;

import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.apirepo.ApiRefPersisterFactory;
import com.blocklang.marketplace.apirepo.RepoPersister;
import com.blocklang.marketplace.apirepo.service.ServiceApiRefPersisterFactory;

public class PersistServiceApiRepoAction extends AbstractPersistApiRepoAction {

	public PersistServiceApiRepoAction(ExecutionContext context) {
		super(context);
	}

	@Override
	protected RepoPersister createApiRepoPersister() {
		ApiRefPersisterFactory factory = new ServiceApiRefPersisterFactory();
		return new RepoPersister(context, factory);
	}

}
