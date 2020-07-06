package com.blocklang.marketplace.apirepo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.runner.common.JsonSchemaValidator;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectContext;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectFactory;
import com.blocklang.marketplace.data.MarketplaceStore;

/**
 * API 仓库解析器
 * 
 * <p>
 * 有 5 个层级的解析，即 repoParser -> refParser -> apiObjectParser -> changelogFileParser -> change。
 * </p>
 * <ul>
 * <li>repoParser - git 仓库级，解析 git 仓库中的所有 tag 和 master 分支</li>
 * <li>refParser - git ref 级，用于解析一个 tag 或 master 分支</li>
 * <li>apiObjectParser - APIObject 级，用于解析一个 API Object（包括 Widget，Service 和 WebApi）</li>
 * <li>changelogFileParser - changelog 文件级，用于解析一个 changelog 文件，一个文件中可包含多个 change</li>
 * <li>change - change 级，用于应用一个变更操作</li>
 * </ul>
 * 
 * <p>这 5 层解析器使用组合或聚合关系，而不是继承关系。</p>
 * 
 * <p>
 * 在 refParser 一级，解析 tag 和 master 分支的流程稍有不同，同时需要一个 ApiObjectContext 存储所有解析的对象。
 * 同时 apiObjectParser 按照 repo 类型分为 Widget、Service 和 WebApi。
 * 其中解析的数据结构是不一样的，但解析流程是一样的。
 * 
 * parser 要设计为无状态的，即一个 parser 对象能在多次循环中重复使用。
 * </p>
 * 
 * @author Zhengwei Jin
 */
public class RepoParser {
	private CliLogger logger;
	private MarketplaceStore store;
	
	private List<String> allTags;
	private List<String> parsedTags = new ArrayList<>();
	
	private ApiObjectFactory factory;
	private RefParser tagParser;
	private RefParser masterParser;
	
	public RepoParser(ExecutionContext context, ApiObjectFactory factory) {
		this.logger = context.getLogger();
		this.store = context.getValue(ExecutionContext.MARKETPLACE_STORE, MarketplaceStore.class);
		this.factory = factory;
	}

	public boolean run() {
		setup();
		
		boolean success = true;
		if(allTags.isEmpty()) {
			logger.info("git 仓库中没有标注 tag");
		} else {
			for(String tag : allTags) {
				if(success) {
					this.tagParser.setFullRefName(tag);
					this.tagParser.setShortRefName(GitUtils.getVersionFromRefName(tag).get());
					
					ParseResult parseResult = this.tagParser.run();
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
		
		// TODO: 如果一个分支解析错误，则要清除为本分支生成的所有文件
		
		if(success) {
			ParseResult parseResult = this.masterParser.run();
			if(parseResult == ParseResult.FAILED) {
				success = false;
				logger.error("解析 master 分支失败");
			}
		}
		
		return success;
	}
	
	private void setup() {
		allTags = getAllTags();
		
		var validator = factory.createSchemaValidator();
		var apiObjectContext = factory.createApiObjectContext(store, logger);
		var changeParserFactory = factory.createChangeParserFactory(logger);
		
		this.tagParser = createTagParser(validator, apiObjectContext, changeParserFactory);
		this.masterParser = createMasterParser(validator, apiObjectContext, changeParserFactory);
	}

	protected RefParser createTagParser(JsonSchemaValidator validator, 
			ApiObjectContext apiObjectContext,
			ChangeParserFactory changeParserFactory) {
		RefParser parser = new TagParser(allTags, store, logger);
		parser.setSchemaValidator(validator);
		parser.setApiObjectContext(apiObjectContext);
		parser.setChangeParserFactory(changeParserFactory);
		return parser;
	}
	
	protected RefParser createMasterParser(JsonSchemaValidator validator, 
			ApiObjectContext apiObjectContext,
			ChangeParserFactory changeParserFactory) {
		RefParser parser = new MasterParser(allTags, store, logger);
		parser.setSchemaValidator(validator);
		parser.setApiObjectContext(apiObjectContext);
		parser.setChangeParserFactory(changeParserFactory);
		return parser;
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
