package com.blocklang.marketplace.apirepo.service;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.JsonSchemaValidator;
import com.blocklang.marketplace.apirepo.ApiObjectContext;
import com.blocklang.marketplace.apirepo.ApiObjectFactory;
import com.blocklang.marketplace.apirepo.ChangeParserFactory;
import com.blocklang.marketplace.data.MarketplaceStore;

public class ServiceFactory extends ApiObjectFactory {

	@Override
	public JsonSchemaValidator createSchemaValidator() {
		return new ServiceChangeSetSchemaValidator();
	}

	@Override
	public ApiObjectContext createApiObjectContext(MarketplaceStore store, CliLogger logger) {
		return new ServiceContext(store, logger);
	}

	@Override
	public ChangeParserFactory createChangeParserFactory(CliLogger logger) {
		return new ServiceChangeParserFactory(logger);
	}

}
