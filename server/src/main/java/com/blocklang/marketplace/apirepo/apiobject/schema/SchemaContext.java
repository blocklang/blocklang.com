package com.blocklang.marketplace.apirepo.apiobject.schema;

import java.nio.file.Path;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.ChangedObjectContext;
import com.blocklang.marketplace.data.MarketplaceStore;

public class SchemaContext extends ChangedObjectContext {

	public SchemaContext(MarketplaceStore store, CliLogger logger) {
		super(store, logger);
	}

	@Override
	protected Path getChangedObjectPath(String previousVersion) {
		return store.getPackageSchemaDirectory(previousVersion);
	}

	@Override
	protected Path getPackageChangelogPath() {
		return store.getPackageSchemaChangelogDirectory();
	}

}
