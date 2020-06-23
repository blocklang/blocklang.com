package com.blocklang.marketplace.apirepo;

import java.util.List;

import com.blocklang.core.runner.common.CliLogger;
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
		
		readAllChangelogFiles();
		
		if(!containChangelogFiles()) {
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
