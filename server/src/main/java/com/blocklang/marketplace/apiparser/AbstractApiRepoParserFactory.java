package com.blocklang.marketplace.apiparser;

import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.runner.common.JsonSchemaValidator;

public abstract class AbstractApiRepoParserFactory {
	
	public abstract JsonSchemaValidator createSchemaValidator();
	
	public abstract TagParser createTagParser(ExecutionContext context);
	
	public abstract MasterParser createMasterParser(ExecutionContext context);
	
}
