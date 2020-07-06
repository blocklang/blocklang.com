package com.blocklang.marketplace.apirepo.apiobject.widget;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.apiobject.ApiObject;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectContext;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.WidgetData;
import com.blocklang.marketplace.data.MarketplaceStore;

public class WidgetContext extends ApiObjectContext{

	public WidgetContext(MarketplaceStore store, CliLogger logger) {
		super(store, logger);
	}

	@Override
	protected Class<? extends ApiObject> getChangedObjectClass() {
		return WidgetData.class;
	}

}
