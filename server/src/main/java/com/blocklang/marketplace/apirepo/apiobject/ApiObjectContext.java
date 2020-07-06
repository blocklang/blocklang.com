package com.blocklang.marketplace.apirepo.apiobject;

import java.nio.file.Path;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.ChangedObjectContext;
import com.blocklang.marketplace.data.MarketplaceStore;

public class ApiObjectContext extends ChangedObjectContext{

	public ApiObjectContext(MarketplaceStore store, CliLogger logger) {
		super(store, logger);
	}

	@Override
	protected Path getChangedObjectPath(String previousVersion) {
		return store.getPackageVersionDirectory(previousVersion);
	}

	@Override
	protected Path getPackageChangelogPath() {
		return store.getPackageChangelogDirectory();
	}
	
}
