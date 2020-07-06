package com.blocklang.marketplace.apirepo.apiobject.widget;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.JsonSchemaValidator;
import com.blocklang.marketplace.apirepo.ChangeParserFactory;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectContext;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectFactory;
import com.blocklang.marketplace.data.MarketplaceStore;

/**
 * 创建解析 Widget 的对象家族
 * 
 * @author Zhengwei Jin
 *
 */
public class WidgetFactory extends ApiObjectFactory {

	@Override
	public JsonSchemaValidator createSchemaValidator() {
		return new WidgetChangeSetSchemaValidator();
	}

	@Override
	public ApiObjectContext createApiObjectContext(MarketplaceStore store, CliLogger logger) {
		return new WidgetContext(store, logger);
	}

	@Override
	public ChangeParserFactory createChangeParserFactory(CliLogger logger) {
		return new WidgetChangeParserFactory(logger);
	}

}
