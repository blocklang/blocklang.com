package com.blocklang.marketplace.runner.action;

import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.apirepo.ApiObjectFactory;
import com.blocklang.marketplace.apirepo.RepoParser;
import com.blocklang.marketplace.apirepo.service.ServiceFactory;

public class ParseServiceApiRepoAction extends AbstractParseApiRepoAction {

	public ParseServiceApiRepoAction(ExecutionContext context) {
		super(context);
	}

	@Override
	protected RepoParser createApiRepoParser() {
		ApiObjectFactory factory = new ServiceFactory();
		return new RepoParser(context, factory);
	}
	
}
