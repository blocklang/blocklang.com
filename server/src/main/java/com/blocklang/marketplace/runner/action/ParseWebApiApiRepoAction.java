package com.blocklang.marketplace.runner.action;

import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.apirepo.RepoParser;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectFactory;
import com.blocklang.marketplace.apirepo.apiobject.webapi.JsObjectFactory;

public class ParseWebApiApiRepoAction extends AbstractParseApiRepoAction {

	public ParseWebApiApiRepoAction(ExecutionContext context) {
		super(context);
	}

	@Override
	protected RepoParser createApiRepoParser() {
		ApiObjectFactory factory = new JsObjectFactory();
		return new RepoParser(context, factory);
	}

}
