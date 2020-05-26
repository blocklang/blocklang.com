package com.blocklang.marketplace.apiparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.data.MarketplaceStore;

public class ApiRepoParser {
	
	private List<String> parsedTags = new ArrayList<String>();
	private boolean success = true;
	
	private TagParser tagParser;
	private MasterParser masterParser;
	
	private CliLogger logger;
	private MarketplaceStore store;

	public ApiRepoParser(ExecutionContext context, AbstractApiRepoParserFactory factory) {
		this.logger = context.getLogger();
		this.store = context.getValue(ExecutionContext.MARKETPLACE_STORE, MarketplaceStore.class);

		ChangeSetSchemaValidator validator = factory.createSchemaValidator();
		
		this.tagParser = factory.createTagParser(context);
		this.tagParser.setChangeSetSchemaValidator(validator);
		
		this.masterParser = factory.createMasterParser(context);
		this.masterParser.setChangeSetSchemaValidator(validator);
	}

	public boolean run() {
		List<String> tags = getAllTags();
		
		if(tags.isEmpty()) {
			logger.info("git 仓库中没有标注 tag");
		} else {
			this.tagParser.setTags(tags);
			for(String tag : tags) {
				if(success) {
					ParseResult parseResult = this.tagParser.run(tag);
					if(parseResult == ParseResult.FAILED) {
						success = false;
						logger.error("解析 {0} 失败", tag);
					} else if(parseResult == ParseResult.ABORT) {
						// do nothing
					} else if(parseResult == ParseResult.SUCCESS) {
						parsedTags.add(tag);
					}
				}
			}
		}
		
		if(success) {
			this.masterParser.setTags(tags);
			ParseResult parseResult = this.masterParser.run();
			if(parseResult == ParseResult.FAILED) {
				success = false;
				logger.error("解析 master 分支失败");
			}
		}
		
		return success;
	}
	
	protected List<String> getAllTags() {
		try {
			return GitUtils.getTags(store.getRepoSourceDirectory())
					.stream()
					.map(ref -> ref.getName())
					.collect(Collectors.toList());
		} catch (RuntimeException e) {
			logger.error(e);
			return Collections.emptyList();
		}
	}

	public List<String> getParsedTags() {
		return this.parsedTags;
	}
	
}
