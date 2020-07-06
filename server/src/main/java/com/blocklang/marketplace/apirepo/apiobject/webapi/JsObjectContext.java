package com.blocklang.marketplace.apirepo.apiobject.webapi;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.apiobject.ApiObject;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectContext;
import com.blocklang.marketplace.apirepo.apiobject.webapi.data.JsObjectData;
import com.blocklang.marketplace.data.MarketplaceStore;

public class JsObjectContext extends ApiObjectContext {

	public JsObjectContext(MarketplaceStore store, CliLogger logger) {
		super(store, logger);
	}

	@Override
	protected Class<? extends ApiObject> getChangedObjectClass() {
		return JsObjectData.class;
	}

}
