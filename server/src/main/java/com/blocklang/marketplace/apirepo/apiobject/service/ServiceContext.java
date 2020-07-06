package com.blocklang.marketplace.apirepo.apiobject.service;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.apiobject.ApiObject;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectContext;
import com.blocklang.marketplace.apirepo.apiobject.service.data.ServiceData;
import com.blocklang.marketplace.data.MarketplaceStore;

public class ServiceContext extends ApiObjectContext {

	public ServiceContext(MarketplaceStore store, CliLogger logger) {
		super(store, logger);
	}

	@Override
	protected Class<? extends ApiObject> getChangedObjectClass() {
		return ServiceData.class;
	}

}
