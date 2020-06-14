package com.blocklang.marketplace.apirepo.service;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.SpringUtils;
import com.blocklang.marketplace.apirepo.ApiRefPersisterFactory;
import com.blocklang.marketplace.apirepo.RefReader;
import com.blocklang.marketplace.apirepo.service.data.ServiceData;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.service.PersistApiRefService;
import com.blocklang.marketplace.service.ServiceApiRefService;

public class ServiceApiRefPersisterFactory extends ApiRefPersisterFactory<ServiceData> {

	@Override
	public RefReader<ServiceData> createRefReader(MarketplaceStore store, CliLogger logger) {
		return new RefReader<ServiceData>(store, logger);
	}

	@Override
	public PersistApiRefService<ServiceData> createPersistApiRefService() {
		return SpringUtils.getBean(ServiceApiRefService.class);
	}

}
