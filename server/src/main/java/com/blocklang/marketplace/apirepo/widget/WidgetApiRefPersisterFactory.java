package com.blocklang.marketplace.apirepo.widget;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.SpringUtils;
import com.blocklang.marketplace.apirepo.ApiRefPersisterFactory;
import com.blocklang.marketplace.apirepo.RefReader;
import com.blocklang.marketplace.apirepo.widget.data.WidgetData;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.service.PersistApiRefService;
import com.blocklang.marketplace.service.WidgetApiRefService;

public class WidgetApiRefPersisterFactory extends ApiRefPersisterFactory<WidgetData> {

	@Override
	public RefReader<WidgetData> createRefReader(MarketplaceStore store, CliLogger logger) {
		return new RefReader<WidgetData>(store, logger);
	}

	@Override
	public PersistApiRefService<WidgetData> createPersistApiRefService() {
		return SpringUtils.getBean(WidgetApiRefService.class);
	}


}
