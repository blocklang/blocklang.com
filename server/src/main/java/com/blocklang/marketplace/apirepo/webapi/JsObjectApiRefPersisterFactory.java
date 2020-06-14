package com.blocklang.marketplace.apirepo.webapi;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.SpringUtils;
import com.blocklang.marketplace.apirepo.ApiRefPersisterFactory;
import com.blocklang.marketplace.apirepo.RefReader;
import com.blocklang.marketplace.apirepo.webapi.data.JsObjectData;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.service.JsObjectApiRefService;
import com.blocklang.marketplace.service.PersistApiRefService;

public class JsObjectApiRefPersisterFactory extends ApiRefPersisterFactory<JsObjectData> {

	@Override
	public RefReader<JsObjectData> createRefReader(MarketplaceStore store, CliLogger logger) {
		return new RefReader<JsObjectData>(store, logger);
	}

	@Override
	public PersistApiRefService<JsObjectData> createPersistApiRefService() {
		return SpringUtils.getBean(JsObjectApiRefService.class);
	}

}
