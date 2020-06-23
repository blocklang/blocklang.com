package com.blocklang.marketplace.apirepo;

import com.blocklang.core.runner.common.CliLogger;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class ChangeParserFactory {

	protected CliLogger logger;

	public ChangeParserFactory(CliLogger logger) {
		this.logger = logger;
	}

	public abstract Change create(JsonNode changeNode);
}
