package com.blocklang.marketplace.apirepo;

import java.util.List;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.schema.RefSchemaParser;
import com.blocklang.marketplace.data.MarketplaceStore;

import de.skuzzle.semantic.Version;

public class TagParser extends RefParser{

	public TagParser(List<String> tags, 
			MarketplaceStore store, 
			CliLogger logger) {
		super(tags, store, logger);
	}
	
	@Override
	public ParseResult run() {
		setup();
		
		if(!isValidStableVersion()) {
			logger.info("{0} 不是稳定的语义版本号，不解析。", shortRefName);
			return ParseResult.ABORT;
		}
		logger.info("开始解析 v{0}", shortRefName);
		
		if(tagParsed()) {
			logger.info("已解析过 v{0}，不再解析", shortRefName);
			return ParseResult.ABORT;
		}
		
		// 解析 schema
		var refSchemaParser = new RefSchemaParser(store, logger, fullRefName, shortRefName, tags);
		var result = refSchemaParser.run();
		if(result == ParseResult.FAILED) {
			return ParseResult.FAILED;
		}
		
		// TODO: 解析 apiObject 中 引用的 schema 在上面是否已定义
		// TODO: 当解析 schema 和 apiObject 到生成文件时出错，则都有可能导致 tagParsed 判断逻辑不准确
		// TODO: 秉承一次解析过程中暴露出所有可能问题的原则，当 schema 解析出错后，依然解析 apiObject
		
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

	// 只解析正式发布版本，不解析 alpha、beta 和 rc 等版本
	private boolean isValidStableVersion() {
		try {
			return Version.parseVersion(shortRefName).isStable();
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean tagParsed() {
		return store.getPackageVersionDirectory(shortRefName).toFile().exists();
	}
}
