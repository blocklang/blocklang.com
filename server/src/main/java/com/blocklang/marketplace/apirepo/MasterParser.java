package com.blocklang.marketplace.apirepo;

import java.io.IOException;
import java.util.List;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.schema.RefSchemaParser;
import com.blocklang.marketplace.data.MarketplaceStore;

public class MasterParser extends RefParser {
	private static final String REF = "refs/heads/";
	private String defaultBranchName;

	public MasterParser(List<String> tags, 
		MarketplaceStore store, 
		CliLogger logger) {
		super(tags, store, logger);

		try {
			this.defaultBranchName = GitUtils.getDefaultBranch(store.getRepoSourceDirectory());
		} catch (IOException e) {
			logger.error("获取默认分支失败");
		}
		setFullRefName(REF + defaultBranchName);
		setShortRefName(defaultBranchName);
	}

	@Override
	public ParseResult run() {
		setup();
		
		if(this.defaultBranchName == null) {
			return ParseResult.FAILED;
		}
		
		logger.info("开始解析 " + defaultBranchName + " 分支");
		
		// 解析 schema
		var refSchemaParser = new RefSchemaParser(store, logger, fullRefName, shortRefName, tags);
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
