package com.blocklang.marketplace.apirepo.schema;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.ChangeParserFactory;
import com.fasterxml.jackson.databind.JsonNode;

public class SchemaChangeParserFactory extends ChangeParserFactory {

	public SchemaChangeParserFactory(CliLogger logger) {
		super(logger);
	}

	@Override
	public Change create(JsonNode changeNode) {
		return null;
	}

}
