package com.blocklang.marketplace.apirepo.apiobject.webapi;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.JsonSchemaValidator;
import com.blocklang.marketplace.apirepo.ApiObjectFactory;
import com.blocklang.marketplace.apirepo.ChangeParserFactory;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectContext;
import com.blocklang.marketplace.data.MarketplaceStore;

public class JsObjectFactory extends ApiObjectFactory {

	@Override
	public JsonSchemaValidator createSchemaValidator() {
		return new JsObjectChangeSetSchemaValidator();
	}

	@Override
	public ApiObjectContext createApiObjectContext(MarketplaceStore store, CliLogger logger) {
		return new JsObjectContext(store, logger);
	}

	@Override
	public ChangeParserFactory createChangeParserFactory(CliLogger logger) {
		return new JsObjectChangeParserFactory(logger);
	}

}
