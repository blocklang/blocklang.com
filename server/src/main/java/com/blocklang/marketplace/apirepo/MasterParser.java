package com.blocklang.marketplace.apirepo;

import java.util.List;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.data.MarketplaceStore;

public class MasterParser extends RefParser {
	private static final String REF = "refs/heads/master";
	private static final String REF_SHORT_NAME = "master";

	public MasterParser(List<String> tags, 
		MarketplaceStore store, 
		CliLogger logger) {
		super(tags, store, logger);

		setFullRefName(REF);
		setShortRefName(REF_SHORT_NAME);
	}

	@Override
	public ParseResult run() {
		setup();
		
		logger.info("开始解析 master 分支");
		
		readAllChangelogFiles();
		
		if(notFoundAnyChangelogFiles()) {
			logger.info("没有找到 changelog 文件，不解析");
			return ParseResult.ABORT;
		}
		
		if(!validateDirAndFileName()) {
			return ParseResult.FAILED;
		}

		if(!validateJsonSchema()) {
			return ParseResult.FAILED;
		}

		if(publishedChangelogFileUpdated()) {
			return ParseResult.FAILED;
		}

		if(!parseAllApiObject()) {
			return ParseResult.FAILED;
		}

		if(!saveAllApiObject()) {
			return ParseResult.FAILED;
		}

		return super.run();
	}

}
