package com.blocklang.marketplace.apiparser;

import java.util.List;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.data.MarketplaceStore;

import de.skuzzle.semantic.Version;

public abstract class TagParser extends AbstractRefParser{

	protected String version;
	
	public TagParser(List<String> tags, MarketplaceStore store, CliLogger logger) {
		super(tags, store, logger);
	}

	public ParseResult run(String fullTagName) {
		version = GitUtils.getVersionFromRefName(fullTagName).orElse(null);
		if(!isValidReleaseVersion(version)) {
			logger.info("{0} 不是稳定的语义版本号，不解析。", version);
			return ParseResult.ABORT;
		}
		logger.info("开始解析 v{0}", version);
		
		if(tagParsed(version)) {
			logger.info("已解析过 v{0}，不再解析", version);
			return ParseResult.ABORT;
		}
		
		readAllChangelogs(fullTagName);
		
		if(!hasChangelogs()) {
			logger.info("没有找到 changelog 文件，不解析", version);
			return ParseResult.ABORT;
		}
		
		if(!validateFileNamePattern()) {
			return ParseResult.FAILED;
		}
		
		if(publishedFileUpdated()) {
			return ParseResult.FAILED;
		}
		
		if(!parseAllApi(fullTagName)) {
			return ParseResult.FAILED;
		}

		if(!saveAllApi()) {
			return ParseResult.FAILED;
		}
		
		return ParseResult.SUCCESS;
	}

	// 只解析正式发布版本，不解析 alpha、beta 和 rc 等版本
	private boolean isValidReleaseVersion(String version) {
		try {
			return Version.parseVersion(version).isStable();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean tagParsed(String version) {
		return store.getPackageVersionDirectory(version).toFile().exists();
	}

}
