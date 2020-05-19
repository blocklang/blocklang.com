package com.blocklang.marketplace.runner.action;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.data.MarketplaceStore;

/**
 * 逐个版本解析 Widget 的 API 仓库，并将解析后的结果存储在文件系统中。
 * 
 * <ul>
 * <li> 准备要解析的 tag 和分支：获取所有 tag 和 master 分支信息
 * <li> 切换到一个版本后，获取所有 change-set json 文件
 * <li> 获取 Widget 列表
 * <li> 逐个 Widget 的解析 change-set json 文件
 * <li> 校验 json schema
 * <li> 保存解析结果
 * </ul>
 * 
 * 注意：此类用于解析所有类型的 API，然后在 run 方法中执行不同的解析操作
 * 
 * <pre>
 * inputs
 *     tags      - string[]，git tag 列表
 *     master    - boolean，是否解析 master 分支，默认为 true
 * outputs
 * 
 * </pre>
 * 
 * @author Zhengwei Jin
 *
 */
public class ParseApiAction extends AbstractAction {
	
	private List<String> tags = Collections.emptyList();
	private boolean master = true;
	
	// FIXME: 临时实现，确保当前 action 的功能跑通
	public static final String INPUT_TAGS = "tags";
	public static final String INPUT_MASTER = "master";

	private MarketplaceStore store;
	private boolean success = true;
	
	public ParseApiAction(ExecutionContext context) {
		super(context);
		
		List<String> inputTags = context.getValue(INPUT_TAGS, List.class);
		if(inputTags != null) {
			tags = inputTags;
		}
		Boolean inputMaster = context.getValue(INPUT_MASTER, Boolean.class);
		if(inputMaster != null) {
			master = inputMaster;
		}
		this.store = context.getValue(ExecutionContext.MARKETPLACE_STORE, MarketplaceStore.class);
	}

	// 不是遇见错误就退出，而是所有文件都要校验一遍
	@Override
	public Optional<?> run() {
		if(tags.isEmpty()) {
			logger.info("git 仓库中没有标注 tag");
		} else {
			TagParser tagParser = new TagParser(tags, store, logger);
			for(String tag : tags) {
				if(success) {
					success = tagParser.run(tag);
				}
			}
		}
		
		if(master && success) {
			MasterBranchParser parser = new MasterBranchParser(tags, store, logger);
			success = parser.run();
		}
		
		return success ? Optional.of(true) : Optional.empty();
	}
}
