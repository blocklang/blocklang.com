package com.blocklang.marketplace.apirepo;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.service.PersistApiRefService;

public abstract class ApiRefPersisterFactory<T extends ApiObject> {
	
	public abstract RefReader<T> createRefReader(MarketplaceStore store, CliLogger logger);
	
	public abstract PersistApiRefService<T> createPersistApiRefService();
}
