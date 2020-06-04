package com.blocklang.marketplace.apirepo.service;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.ApiObject;
import com.blocklang.marketplace.apirepo.ApiObjectContext;
import com.blocklang.marketplace.apirepo.service.data.ServiceData;
import com.blocklang.marketplace.data.MarketplaceStore;

public class ServiceContext extends ApiObjectContext {

	public ServiceContext(MarketplaceStore store, CliLogger logger) {
		super(store, logger);
	}

	@Override
	protected Class<? extends ApiObject> getApiObjectClass() {
		return ServiceData.class;
	}

}
