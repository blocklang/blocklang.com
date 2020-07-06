package com.blocklang.marketplace.apirepo;

import java.util.List;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.schema.RefSchemaParser;
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
		
		// 解析 schema
		var refSchemaParser = new RefSchemaParser(store, logger, fullRefName, shortRefName);
		var result = refSchemaParser.run();
		if(result == ParseResult.FAILED) {
			return ParseResult.FAILED;
		}
		
		// 解析 apiObject
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
