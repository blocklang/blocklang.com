package com.blocklang.marketplace.runner.action;

import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.apirepo.ApiRefPersisterFactory;
import com.blocklang.marketplace.apirepo.RepoPersister;
import com.blocklang.marketplace.apirepo.webapi.JsObjectApiRefPersisterFactory;

public class PersistWebApiApiRepoAction extends AbstractPersistApiRepoAction {

	public PersistWebApiApiRepoAction(ExecutionContext context) {
		super(context);
	}

	@Override
	protected RepoPersister createApiRepoPersister() {
		ApiRefPersisterFactory factory = new JsObjectApiRefPersisterFactory();
		return new RepoPersister(context, factory);
	}

}
