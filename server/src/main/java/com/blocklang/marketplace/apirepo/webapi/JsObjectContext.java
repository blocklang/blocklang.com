package com.blocklang.marketplace.apirepo.webapi;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.ApiObject;
import com.blocklang.marketplace.apirepo.ApiObjectContext;
import com.blocklang.marketplace.apirepo.webapi.data.JsObjectData;
import com.blocklang.marketplace.data.MarketplaceStore;

public class JsObjectContext extends ApiObjectContext {

	public JsObjectContext(MarketplaceStore store, CliLogger logger) {
		super(store, logger);
	}

	@Override
	protected Class<? extends ApiObject> getApiObjectClass() {
		return JsObjectData.class;
	}

}
