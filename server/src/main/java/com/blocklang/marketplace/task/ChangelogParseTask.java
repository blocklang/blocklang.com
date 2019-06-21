package com.blocklang.marketplace.task;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.blocklang.develop.constant.AppType;
import com.blocklang.marketplace.constant.ComponentAttrValueType;
import com.blocklang.marketplace.data.changelog.ChangeLog;

/**
 * TODO: 如何重构这个类，目前还没有思路，本版以实现功能为主，还做不到高可读、可扩展和通用。
 * 也许可以使用或借鉴 java-json-tools
 * 
 * @author Zhengwei Jin
 *
 */
public class ChangelogParseTask extends AbstractRepoPublishTask {

	private Map<?,?> changelogMap;
	private List<String> childKeysForRoot = Arrays.asList("id", "author", "changes");
	private List<String> operators = Arrays.asList("newWidget");
	private List<String> newWidgetKeys = Arrays.asList("name", "label", "iconClass", "appType", "properties", "events");
	private List<String> widgetPropertyKeys = Arrays.asList("name", "label", "value", "valueType", "options");
	private List<String> widgetEventKeys = Arrays.asList("name", "label", "valueType", "arguments");
	private List<String> widgetPropertyOptionKeys = Arrays.asList("value", "label", "title", "iconClass");
	private List<String> widgetEventArgumentKeys = Arrays.asList("name", "label", "value", "valueType");
	/**
	 * @see AppType
	 */
	private List<String> appTypes = Arrays.asList("web", "wechat");
	/**
	 * @see ComponentAttrValueType
	 * 
	 * 注意，function 的相关逻辑单独处理了，所以这里不需要包含 function
	 */
	private List<String> valueTypes = Arrays.asList("string", "number", "boolean");
	
	public ChangelogParseTask(MarketplacePublishContext marketplacePublishContext, Map<?, ?> changelogMap) {
		super(marketplacePublishContext);
		this.changelogMap = changelogMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
		if(changesObj == null || !List.class.isAssignableFrom(changesObj.getClass())) {
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
					logger.error("第 {0} 个元素：不支持的操作，当前只支持 {1}", index + 1, String.join("、", operators));
					hasErrors = true;
				}
			}
			index++;
		}
		
		if(hasErrors) {
			return Optional.empty();
		}
		
		hasErrors = false;
		index = 0;
		for(Object changeObj : changeList) {
			Map changeMap = (Map)changeObj;
			if(changeMap.containsKey("newWidget")) {
				Map newWidgetMap = (Map)changeMap.get("newWidget");
				for(Object key : newWidgetMap.keySet()) {
					if(!newWidgetKeys.contains(key)) {
						logger.error("newWidget 节点下只支持 {0}，不支持 {1}", String.join("、", newWidgetKeys.toArray(new String[0])), key);
						hasErrors = true;
					}
				}
			}
			index++;
		}
		if(hasErrors) {
			return Optional.empty();
		}
		
		hasErrors = false;
		index = 0;
		for(Object changeObj : changeList) {
			Map changeMap = (Map)changeObj;
			if(changeMap.containsKey("newWidget")) {
				Map newWidgetMap = (Map)changeMap.get("newWidget");
				// name
				Object nameObj = newWidgetMap.get("name");
				// name 不能为空
				if(nameObj == null || !String.class.isAssignableFrom(nameObj.getClass())) {
					logger.error("name 的值不能为空，且必须是字符串类型");
					hasErrors = true;
				}
				
				// label
				Object labelObj = newWidgetMap.get("label");
				if(labelObj != null && !String.class.isAssignableFrom(labelObj.getClass())) {
					logger.error("label 的值必须是字符串类型");
					hasErrors = true;
				}
				
				// iconClass
				Object iconClassObj = newWidgetMap.get("iconClass");
				if(iconClassObj != null && !String.class.isAssignableFrom(iconClassObj.getClass())) {
					logger.error("iconClass 的值必须是字符串类型");
					hasErrors = true;
				}
				
				// appType
				Object appTypeObj = newWidgetMap.get("appType");
				if(appTypeObj == null || !List.class.isAssignableFrom(appTypeObj.getClass())) {
					logger.error("appType 的值必须是字符串类型的数组");
					hasErrors = true;
				} else {
					List<Object> appTypeList = (List<Object>)appTypeObj;
					for(Object appType : appTypeList) {
						if(!this.appTypes.contains(appType)) {
							logger.error("appType 的值只能是 {0}，不能是 {1}", String.join("、", this.appTypes), appType);
							hasErrors = true;
						}
					}
				}
				
				// properties
				Object propertiesObj = newWidgetMap.get("properties");
				if(propertiesObj != null) {
					if(!List.class.isAssignableFrom(propertiesObj.getClass())) {
						logger.error("properties 的值必须是 JSON 数组");
						hasErrors = true;
					} else {
						List<Object> propertyList = (List<Object>)propertiesObj;
						int j = 0;
						for(Object property : propertyList) {
							if(!Map.class.isAssignableFrom(property.getClass())) {
								logger.error("properties 中的第 {0} 元素不是有效的 JSON 对象", j+1);
								hasErrors = true;
							} else {
								Map propertyMap = (Map)property;
								for(Object key : propertyMap.keySet()) {
									if(!widgetPropertyKeys.contains(key)) {
										logger.error("properties 中的第 {0} 个元素中包含不支持的 key，当前只支持 {1}，不支持 {2}", 
												j + 1,
												String.join("、", widgetPropertyKeys),
												key);
										hasErrors = true;
									}
								}
								
								if(!hasErrors) {
									// name
									Object propNameObj = propertyMap.get("name");
									if(propNameObj == null || !String.class.isAssignableFrom(propNameObj.getClass())) {
										logger.error("name 的值不能为空，且必须是字符串类型");
										hasErrors = true;
									}
									// label
									Object propLabelObj = propertyMap.get("label");
									if(propLabelObj != null && !String.class.isAssignableFrom(propLabelObj.getClass())) {
										logger.error("label 的值必须是字符串类型");
										hasErrors = true;
									}
									// value
									Object propValueObj = propertyMap.get("value");
									if(propValueObj != null && !String.class.isAssignableFrom(propValueObj.getClass())) {
										logger.error("value 的值必须是字符串类型");
										hasErrors = true;
									}
									// valueType
									Object propValueTypeObj = propertyMap.get("valueType");
									if(propValueTypeObj == null || !String.class.isAssignableFrom(propValueTypeObj.getClass())) {
										logger.error("valueType 的值必须是字符串类型");
										hasErrors = true;
									} else {
										if(!valueTypes.contains(propValueTypeObj)) {
											logger.error("valueType 的值只能是 {0}，不支持 {1}", String.join("、", valueTypes), propertiesObj);
											hasErrors = true;
										}
									}
									// options(不是必填项)
									Object propOptionsObj = propertyMap.get("options");
									if(propOptionsObj != null) {
										if(!List.class.isAssignableFrom(propOptionsObj.getClass())) {
											logger.error("options 的值必须是 JSON 数组");
											hasErrors = true;
										}else {
											List<Object> propOptionList = (List<Object>)propOptionsObj;
											int k = 0;
											for(Object propOption : propOptionList) {
												Map propOptionMap = (Map)propOption;
												for(Object key : propOptionMap.keySet()) {
													if(!widgetPropertyOptionKeys.contains(key)) {
														logger.error("options 中的第 {0} 个元素中包含不支持的 key，当前只支持 {1}，不支持 {2}", 
																k + 1,
																String.join("、", widgetPropertyOptionKeys),
																key);
														hasErrors = true;
													}
												}
												if(!hasErrors) {
													// value
													Object propOptionValueObj = propOptionMap.get("value");
													if(propOptionValueObj == null || !String.class.isAssignableFrom(propOptionValueObj.getClass())) {
														logger.error("value 的值不能为空，且必须是字符串类型");
														hasErrors = true;
													}
													// label
													Object propOptionLabelObj = propOptionMap.get("label");
													if(propOptionLabelObj == null || !String.class.isAssignableFrom(propOptionLabelObj.getClass())) {
														logger.error("label 的值不能为空，且必须是字符串类型");
														hasErrors = true;
													}
													// title
													Object propOptionTitleObj = propOptionMap.get("title");
													if(propOptionTitleObj != null && !String.class.isAssignableFrom(propOptionTitleObj.getClass())) {
														logger.error("title 的值必须是字符串类型");
														hasErrors = true;
													}
													// iconClass
													Object propOptionIconClassObj = propOptionMap.get("iconClass");
													if(propOptionIconClassObj != null && !String.class.isAssignableFrom(propOptionIconClassObj.getClass())) {
														logger.error("iconClass 的值必须是字符串类型");
														hasErrors = true;
													}
												}
												
												k++;
											}
										}
									}
								}
							}
							j++;
						}
					}
				}
				
				// events
				Object eventsObj = newWidgetMap.get("events");
				if(eventsObj != null) {
					if(!List.class.isAssignableFrom(eventsObj.getClass())) {
						logger.error("events 的值必须是 JSON 数组");
						hasErrors = true;
					} else {
						List<Object> eventList = (List<Object>)eventsObj;
						int j = 0;
						for(Object event : eventList) {
							if(!Map.class.isAssignableFrom(event.getClass())) {
								logger.error("events 中的第 {0} 元素不是有效的 JSON 对象", j+1);
								hasErrors = true;
							} else {
								Map eventMap = (Map)event;
								for(Object key : eventMap.keySet()) {
									if(!widgetEventKeys.contains(key)) {
										logger.error("events 中的第 {0} 个元素中包含不支持的 key，当前只支持 {1}，不支持 {2}", 
												j + 1,
												String.join("、", widgetEventKeys),
												key);
										hasErrors = true;
									}
								}
								
								if(!hasErrors) {
									// name
									Object eventNameObj = eventMap.get("name");
									if(eventNameObj == null || !String.class.isAssignableFrom(eventNameObj.getClass())) {
										logger.error("name 的值不能为空，且必须是字符串类型");
										hasErrors = true;
									}
									// label
									Object eventLabelObj = eventMap.get("label");
									if(eventLabelObj == null || !String.class.isAssignableFrom(eventLabelObj.getClass())) {
										logger.error("label 的值不能为空，且必须是字符串类型");
										hasErrors = true;
									}
									// valueType(可不填，默认是 function)
									Object eventValueTypeObj = eventMap.get("valueType");
									if(eventValueTypeObj != null && !String.class.isAssignableFrom(eventValueTypeObj.getClass())) {
										logger.error("valueType 必须是字符串类型");
										hasErrors = true;
									} else {
										if(!"function".equals(eventValueTypeObj)) {
											logger.error("valueType 的值只能是 function。默认为 function，可不填写");
											hasErrors = true;
										}
									}
									// arguments
									Object eventArgumentsObj = eventMap.get("arguments");
									if(eventArgumentsObj != null) {
										if(!List.class.isAssignableFrom(eventArgumentsObj.getClass())) {
											logger.error("arguments 的值必须是 JSON 数组");
											hasErrors = true;
										} else {
											List<Object> eventArgumentList = (List<Object>)eventArgumentsObj;
											int k = 0;
											for(Object eventArgument : eventArgumentList) {
												if(!Map.class.isAssignableFrom(eventArgument.getClass())) {
													logger.error("arguments 中的第 {0} 元素不是有效的 JSON 对象", k+1);
													hasErrors = true;
												} else {
													Map eventArgumentMap = (Map)eventArgument;
													for(Object key : eventArgumentMap.keySet()) {
														if(!widgetEventArgumentKeys.contains(key)) {
															logger.error("arguments 中的第 {0} 个元素中包含不支持的 key，当前只支持 {1}，不支持 {2}", 
																	k + 1,
																	String.join("、", widgetEventArgumentKeys),
																	key);
															hasErrors = true;
														}
													}
													
													if(!hasErrors) {
														// name
														Object eventArgumentNameObj = eventArgumentMap.get("name");
														if(eventArgumentNameObj == null || !String.class.isAssignableFrom(eventArgumentNameObj.getClass())) {
															logger.error("name 的值不能为空，且必须是字符串类型");
															hasErrors = true;
														}
														
														// label
														Object eventArgumentLabelObj = eventArgumentMap.get("label");
														if(eventArgumentLabelObj == null || !String.class.isAssignableFrom(eventArgumentLabelObj.getClass())) {
															logger.error("label 的值不能为空，且必须是字符串类型");
															hasErrors = true;
														}
														
														// value
														Object eventArgumentValueObj = eventArgumentMap.get("value");
														if(eventArgumentValueObj != null && !String.class.isAssignableFrom(eventArgumentValueObj.getClass())) {
															logger.error("value 的值必须是字符串类型");
															hasErrors = true;
														}
														// valueType
														Object eventArgumentValueTypeObj = eventArgumentMap.get("valueType");
														if(eventArgumentValueTypeObj != null && !String.class.isAssignableFrom(eventArgumentValueTypeObj.getClass())) {
															logger.error("valueType 的值必须是字符串类型");
															hasErrors = true;
														} else {
															if(!this.valueTypes.contains(eventArgumentValueTypeObj)) {
																logger.error("valueType 的值只能是 {0}，不支持 {1}", String.join("、", valueTypes), eventArgumentValueTypeObj);
																hasErrors = true;
															}
														}
													}
												}
												
												k++;
											}
										}
									}
								}
							}
							
							j++;
						}
					}
				}
				
			}
			index++;
		}
		if(hasErrors) {
			return Optional.empty();
		}
		
		
		
		return Optional.of(new ChangeLog());
	}

}
