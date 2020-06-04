package com.blocklang.marketplace.apirepo.widget;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.ApiObject;
import com.blocklang.marketplace.apirepo.ApiObjectContext;
import com.blocklang.marketplace.apirepo.widget.data.WidgetData;
import com.blocklang.marketplace.data.MarketplaceStore;

public class WidgetContext extends ApiObjectContext{

	public WidgetContext(MarketplaceStore store, CliLogger logger) {
		super(store, logger);
	}

	@Override
	protected Class<? extends ApiObject> getApiObjectClass() {
		return WidgetData.class;
	}

}
