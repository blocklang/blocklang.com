package com.blocklang.marketplace.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.blocklang.develop.constant.AppType;
import com.blocklang.marketplace.constant.ComponentAttrValueType;
import com.blocklang.marketplace.data.changelog.Change;
import com.blocklang.marketplace.data.changelog.ChangeLog;
import com.blocklang.marketplace.data.changelog.NewWidgetChange;
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.data.changelog.WidgetEventArgument;
import com.blocklang.marketplace.data.changelog.WidgetProperty;
import com.blocklang.marketplace.data.changelog.WidgetPropertyOption;

/**
 * TODO: 如何重构这个类，目前还没有思路，本版以实现功能为主，还做不到高可读、可扩展和通用。
 * 也许可以使用或借鉴 java-json-tools
 * 
 * @author Zhengwei Jin
 *
 */
public class ApiChangeLogValidateTask extends AbstractRepoPublishTask {

	private Map<?,?> changelogMap;
	private List<String> childKeysForRoot = Arrays.asList("id", "author", "changes");
	private List<String> operators = Arrays.asList("newWidget");
	private List<String> newWidgetKeys = Arrays.asList("name", "label", "description", "iconClass", "appType", "properties", "events");
	private List<String> widgetPropertyKeys = Arrays.asList("name", "label", "value", "valueType", "description", "options");
	private List<String> widgetEventKeys = Arrays.asList("name", "label", "valueType", "description", "arguments");
	private List<String> widgetPropertyOptionKeys = Arrays.asList("value", "label", "description", "valueDescription", "iconClass");
	private List<String> widgetEventArgumentKeys = Arrays.asList("name", "label", "value", "valueType", "description");
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
	
	public ApiChangeLogValidateTask(MarketplacePublishContext marketplacePublishContext, Map<?, ?> changelogMap) {
		super(marketplacePublishContext);
		this.changelogMap = changelogMap;
	}
	
	/**
	 * 将校验和解析两步完全分离，虽然会重复循环，但代码易读。
	 */
	@Override
	public Optional<ChangeLog> run() {
		if(this.changelogMap == null) {
			logger.error("changelogMap 参数不能为 null");
			return Optional.empty();
		}
		
		if(!validate()) {
			return Optional.empty();
		}
		
		return Optional.of(this.parse());
	}
	
	/**
	 * 如果校验未通过，则返回 false，否则返回 true
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean validate() {
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
			return false;
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
			return false;
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
			return false;
		}
		
		List changeList = (List) changelogMap.get("changes");
		if(changeList.isEmpty()) {
			logger.error("changes 数组中没有任何内容，至少要包含一项内容");
			return false;
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
			return false;
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
			return false;
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
				
				// description
				Object descriptionObj = newWidgetMap.get("description");
				if(descriptionObj != null && !String.class.isAssignableFrom(descriptionObj.getClass())) {
					logger.error("description 的值必须是字符串类型");
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
									// description
									Object propDescriptionObj = propertyMap.get("description");
									if(propDescriptionObj != null && !String.class.isAssignableFrom(propDescriptionObj.getClass())) {
										logger.error("description 的值必须是字符串类型");
										hasErrors = true;
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
													// description
													Object propOptionDescriptionObj = propOptionMap.get("description");
													if(propOptionDescriptionObj != null && !String.class.isAssignableFrom(propOptionDescriptionObj.getClass())) {
														logger.error("description 的值必须是字符串类型");
														hasErrors = true;
													}
													// value description
													Object propOptionValueDescriptionObj = propOptionMap.get("valueDescription");
													if(propOptionValueDescriptionObj != null && !String.class.isAssignableFrom(propOptionValueDescriptionObj.getClass())) {
														logger.error("valueDescription 的值必须是字符串类型");
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
									if(eventValueTypeObj != null) {
										if(!String.class.isAssignableFrom(eventValueTypeObj.getClass())) {
											logger.error("valueType 必须是字符串类型");
											hasErrors = true;
										} else {
											if(!"function".equals(eventValueTypeObj)) {
												logger.error("valueType 的值只能是 function。默认为 function，可不填写");
												hasErrors = true;
											}
										}
									}
									// description
									Object eventDescriptionObj = eventMap.get("description");
									if(eventDescriptionObj != null && !String.class.isAssignableFrom(eventDescriptionObj.getClass())) {
										logger.error("description 的值必须是字符串类型");
										hasErrors = true;
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
														
														// description
														Object eventArgumentDescriptionObj = eventArgumentMap.get("description");
														if(eventArgumentDescriptionObj != null && !String.class.isAssignableFrom(eventArgumentDescriptionObj.getClass())) {
															logger.error("description 的值必须是字符串类型");
															hasErrors = true;
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
			return false;
		}
		
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ChangeLog parse() {
		ChangeLog changelog = new ChangeLog();
		changelog.setId(Objects.toString(changelogMap.get("id"), ""));
		changelog.setAuthor(Objects.toString(changelogMap.get("author"), ""));
		
		List<Change> changes = new ArrayList<Change>();
		
		List<Object> changeList = (List<Object>) changelogMap.get("changes");
		for(Object changeObj : changeList) {
			Map changeMap = (Map)changeObj;
			if(changeMap.containsKey("newWidget")) {
				Map newWidgetMap = (Map) changeMap.get("newWidget");
				NewWidgetChange newWidgetChange = new NewWidgetChange();
				newWidgetChange.setName(newWidgetMap.getOrDefault("name", "").toString());
				newWidgetChange.setLabel(newWidgetMap.getOrDefault("label", "").toString());
				
				Object widgetDescription = newWidgetMap.get("description");
				if(widgetDescription != null) {
					newWidgetChange.setDescription(widgetDescription.toString());
				}
				
				Object iconClassObj = newWidgetMap.get("iconClass");
				if(iconClassObj != null) {
					newWidgetChange.setIconClass(iconClassObj.toString());
				}
				
				newWidgetChange.setAppType((List<String>) newWidgetMap.get("appType"));
				
				List<WidgetProperty> properties = new ArrayList<WidgetProperty>();
				Object propertyObj = newWidgetMap.get("properties");
				if(propertyObj != null) {
					List<Map> propertyList = (List<Map>)propertyObj;
					for(Map propertyMap : propertyList) {
						WidgetProperty widgetProperty = new WidgetProperty();
						widgetProperty.setName(propertyMap.get("name").toString());
						widgetProperty.setLabel(propertyMap.get("label").toString());
						
						Object propertyValue = propertyMap.get("value");
						if(propertyValue != null) {
							widgetProperty.setValue(propertyValue.toString());
						}
						
						widgetProperty.setValueType(propertyMap.get("valueType").toString());
						
						Object propertyDescription = propertyMap.get("description");
						if(propertyDescription != null) {
							widgetProperty.setDescription(propertyDescription.toString());
						}
						
						List<WidgetPropertyOption> options = new ArrayList<WidgetPropertyOption>();
						Object optionObj = propertyMap.get("options");
						if(optionObj != null) {
							List<Map> optionList = (List<Map>)optionObj;
							for(Map optionMap : optionList) {
								WidgetPropertyOption option = new WidgetPropertyOption();
								option.setValue(optionMap.get("value").toString());
								option.setLabel(optionMap.get("label").toString());
								
								Object descriptionObj = optionMap.get("description");
								if(descriptionObj != null) {
									option.setDescription(descriptionObj.toString());
								}
								
								Object valueDescriptionObj = optionMap.get("valueDescription");
								if(valueDescriptionObj != null) {
									option.setValueDescription(valueDescriptionObj.toString());
								}
								
								Object optionIconClassObj = optionMap.get("iconClass");
								if(optionIconClassObj != null) {
									option.setIconClass(optionIconClassObj.toString());
								}
								
								options.add(option);
							}
						}
						widgetProperty.setOptions(options);
						
						properties.add(widgetProperty);
					}
				}
				newWidgetChange.setProperties(properties);
				
				List<WidgetEvent> events = new ArrayList<WidgetEvent>();
				Object eventObj = newWidgetMap.get("events");
				if(eventObj != null) {
					List<Map> eventList = (List<Map>)eventObj;
					for(Map eventMap : eventList) {
						WidgetEvent widgetEvent = new WidgetEvent();
						widgetEvent.setName(eventMap.get("name").toString());
						widgetEvent.setLabel(eventMap.get("label").toString());
						
						Object valueTypeObj = eventMap.get("valueType");
						if(valueTypeObj == null) {
							widgetEvent.setValueType("function");
						}else {
							widgetEvent.setValueType(valueTypeObj.toString());
						}
						
						Object descriptionObj = eventMap.get("description");
						if(descriptionObj != null) {
							widgetEvent.setDescription(descriptionObj.toString());
						}
						
						List<WidgetEventArgument> arguments = new ArrayList<WidgetEventArgument>();
						Object argumentObj = eventMap.get("arguments");
						if(argumentObj != null) {
							List<Map> argumentList = (List<Map>)argumentObj;
							for(Map argumentMap : argumentList) {
								WidgetEventArgument argument = new WidgetEventArgument();
								argument.setName(argumentMap.get("name").toString());
								argument.setLabel(argumentMap.get("label").toString());
								argument.setValue(argumentMap.get("value").toString());
								argument.setValueType(argumentMap.get("valueType").toString());
								
								Object argumentDescriptionObj = argumentMap.get("description");
								if(argumentDescriptionObj != null) {
									argument.setDescription(argumentDescriptionObj.toString());
								}
								
								arguments.add(argument);
							}
						}
						widgetEvent.setArguments(arguments);
						
						
						events.add(widgetEvent);
					}
				}
				newWidgetChange.setEvents(events);
				
				changes.add(newWidgetChange);
			}
		}
		
		changelog.setChanges(changes);
		
		return changelog;
	}
}
