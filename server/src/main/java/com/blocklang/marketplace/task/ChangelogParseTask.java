package com.blocklang.marketplace.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.blocklang.marketplace.data.changelog.ChangeLog;

public class ChangelogParseTask extends AbstractRepoPublishTask {

	private Map<?,?> changelogMap;
	private List<String> childKeysForRoot = Arrays.asList("id", "author", "changes");
	private List<String> operators = Arrays.asList("newWidget");
	
	public ChangelogParseTask(MarketplacePublishContext marketplacePublishContext, Map<?,?> changelogMap) {
		super(marketplacePublishContext);
		this.changelogMap = changelogMap;
	}

	@Override
	public Optional<ChangeLog> run() {
		if(this.changelogMap == null) {
			logger.error("changelogMap 参数不能为 null");
			return Optional.empty();
		}
		// 根结点下必须包含 id,author,changes 子节点
		// 1. 只能是 id,author,changes
		boolean hasErrors = false;
		for(Object key : changelogMap.keySet()) {
			if(!childKeysForRoot.contains(key)) {
				logger.error("只支持 id、author 和 changes 三个key。不支持 {0}", key);
				hasErrors = true;
			}
		}
		if(hasErrors) {
			return Optional.empty();
		}
		
		hasErrors = false;
		// 2. 且必须存在 id,author,changes
		// 错误信息中用 jsonpath 表示
		if(changelogMap.keySet().stream().noneMatch(key -> key.equals("id"))) {
			logger.error("缺少 /id");
			hasErrors = true;
		}
		if(changelogMap.keySet().stream().noneMatch(key -> key.equals("author"))) {
			logger.error("缺少 /author");
			hasErrors = true;
		}
		if(changelogMap.keySet().stream().noneMatch(key -> key.equals("changes"))) {
			logger.error("缺少 /changes");
			hasErrors = true;
		}
		if(hasErrors) {
			return Optional.empty();
		}
		
		hasErrors = false;
		Object idObj = changelogMap.get("id");
		if(idObj == null || !String.class.isAssignableFrom(idObj.getClass())) {
			logger.error("id 的值必须是字符串类型");
			hasErrors = true;
		}
		
		Object authorObj = changelogMap.get("author");
		if(authorObj == null || !String.class.isAssignableFrom(authorObj.getClass())) {
			logger.error("author 的值必须是字符串类型");
			hasErrors = true;
		}
		
		Object changesObj = changelogMap.get("changes");
		if(changesObj == null || !ArrayList.class.isAssignableFrom(changesObj.getClass())) {
			logger.error("changes 的值必须是数组类型");
			hasErrors = true;
		}
		if(hasErrors) {
			return Optional.empty();
		}
		
		List changeList = (List) changelogMap.get("changes");
		if(changeList.isEmpty()) {
			logger.error("changes 数组中没有任何内容，至少要包含一项内容");
			return Optional.empty();
		}
		
		hasErrors = false;
		int index = 0;
		for(Object changeObj : changeList) {
			Map changeMap = (Map)changeObj;
			if(changeMap.size() > 1) {
				logger.error("第 {0} 个元素：包含了 {1} 个操作，只能包含一个操作", index + 1, changeMap.size());
				hasErrors = true;
			}
			index++;
		}
		index = 0;
		for(Object changeObj : changeList) {
			Map changeMap = (Map)changeObj;
			for(Object key : changeMap.keySet()) {
				if(!operators.contains(key)) {
					logger.error("第 {0} 个元素：不支持的操作，当前只支持 {1}", index + 1, operators.size());
					hasErrors = true;
				}
			}
			index++;
		}
		
		if(hasErrors) {
			return Optional.empty();
		}
		
		hasErrors = false;
		
		return null;
	}

}
