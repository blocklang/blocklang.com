package com.blocklang.marketplace.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.blocklang.core.util.StringUtils;
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
	private List<String> widgetPropertyKeys = Arrays.asList("name", "label", "defaultValue", "valueType", "description", "options");
	private List<String> widgetEventKeys = Arrays.asList("name", "label", "valueType", "description", "arguments");
	private List<String> widgetPropertyOptionKeys = Arrays.asList("value", "label", "description", "iconClass");
	private List<String> widgetEventArgumentKeys = Arrays.asList("name", "label", "defaultValue", "valueType", "description");
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
	@SuppressWarnings({ "rawtypes" })
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
		String rootIdKeyName = "id";
		Object idObj = changelogMap.get(rootIdKeyName);
		if(idObj == null || !String.class.isAssignableFrom(idObj.getClass())) {
			logger.error("/{0} 的值必须是字符串类型", rootIdKeyName);
			hasErrors = true;
		} else {
			// 校验值的长度
			if(hasMaxLengthError("/" + rootIdKeyName, idObj.toString(), 255)) {
				hasErrors = true;
			}
		}
		
		String rootAuthorKeyName = "author";
		Object authorObj = changelogMap.get(rootAuthorKeyName);
		if(authorObj == null || !String.class.isAssignableFrom(authorObj.getClass())) {
			logger.error("/{0} 的值必须是字符串类型", rootAuthorKeyName);
			hasErrors = true;
		} else {
			// 校验值的长度
			if(hasMaxLengthError("/" + rootAuthorKeyName, authorObj.toString(), 255)) {
				hasErrors = true;
			}
		}
		
		Object changesObj = changelogMap.get("changes");
		if(changesObj == null || !List.class.isAssignableFrom(changesObj.getClass())) {
			logger.error("/changes 的值必须是数组类型");
			hasErrors = true;
		}
		if(hasErrors) {
			return false;
		}
		
		List changeList = (List) changelogMap.get("changes");
		if(changeList.isEmpty()) {
			logger.error("/changes 数组中没有任何内容，至少要包含一项内容");
			return false;
		}
		
		hasErrors = false;
		int index = 0;
		for(Object changeObj : changeList) {
			Map changeMap = (Map)changeObj;
			if(changeMap.size() > 1) {
				logger.error("/changes 的第 {0} 个元素：包含了 {1} 个操作，只能包含一个操作", index + 1, changeMap.size());
				hasErrors = true;
			}
			index++;
		}
		index = 0;
		for(Object changeObj : changeList) {
			Map changeMap = (Map)changeObj;
			for(Object key : changeMap.keySet()) {
				if(!operators.contains(key)) {
					logger.error("/changes 的第 {0} 个元素：不支持的操作，当前只支持 {1}", index + 1, String.join("、", operators));
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
						logger.error("/changes 的第 {0} 个元素 newWidget 节点下只支持 {1}，不支持 {2}", index + 1, String.join("、", newWidgetKeys.toArray(new String[0])), key);
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
				logger.info("校验 /changes 的第 {0} 个元素 newWidget 节点下的元素", index + 1);

				hasErrors = validateNewWidget(newWidgetMap);
			}
			index++;
		}
		if(hasErrors) {
			return false;
		}
		
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean validateNewWidget(Map newWidgetMap) {
		boolean hasErrors = false;
		String nameFieldKey = "name";
		// name
		Object nameObj = newWidgetMap.get(nameFieldKey);
		// name 不能为空
		if(nameObj == null || !String.class.isAssignableFrom(nameObj.getClass())) {
			logger.error("{0} 的值不能为空，且必须是字符串类型", nameFieldKey);
			hasErrors = true;
		}else {
			if(hasMaxLengthError(nameFieldKey, nameObj.toString(), 64)) {
				hasErrors = true;
			}else {
				// 不能写 hasErrors = false;
				// 注意：只能将 hasErrors 的值设置为 true，不能设置为 false
				// 因为这里要寻找是否存在错误，不能让某一个字段的对，覆盖了全局的 hasErrors 值
			}
		}
		
		// label
		String labelFieldKey = "label";
		Object labelObj = newWidgetMap.get(labelFieldKey);
		if(labelObj != null) {
			if(!String.class.isAssignableFrom(labelObj.getClass())) {
				logger.error("{0} 的值必须是字符串类型", labelFieldKey);
				hasErrors = true;
			}else {
				if(hasMaxLengthError(labelFieldKey, labelObj.toString(), 64)) {
					hasErrors = true;
				}
			}
		}
		
		// description
		String descriptionFieldKey = "description";
		Object descriptionObj = newWidgetMap.get(descriptionFieldKey);
		if(descriptionObj != null) {
			if(!String.class.isAssignableFrom(descriptionObj.getClass())) {
				logger.error("{0} 的值必须是字符串类型", descriptionFieldKey);
				hasErrors = true;
			}else {
				if(hasMaxLengthError(descriptionFieldKey, descriptionObj.toString(), 512)) {
					hasErrors = true;
				}
			}
		}
		
		
		// iconClass
		Object iconClassObj = newWidgetMap.get("iconClass");
		if(iconClassObj != null && !String.class.isAssignableFrom(iconClassObj.getClass())) {
			logger.error("iconClass 的值必须是字符串类型");
			hasErrors = true;
		}
		
		// appType
		// TODO: 目前还未确定 appType 放在此处是否合适，先将此字段按非必填处理
		Object appTypeObj = newWidgetMap.get("appType");
		if(appTypeObj != null) {
			if(!List.class.isAssignableFrom(appTypeObj.getClass())) {
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
							String propNameKey = "name";
							Object propNameObj = propertyMap.get(propNameKey);
							if(propNameObj == null || !String.class.isAssignableFrom(propNameObj.getClass())) {
								logger.error("{0} 的值不能为空，且必须是字符串类型", propNameKey);
								hasErrors = true;
							} else {
								if(hasMaxLengthError(propNameKey, propNameObj.toString(), 64)) {
									hasErrors = true;
								}
							}
							
							// label
							String propLabelKey = "label";
							Object propLabelObj = propertyMap.get(propLabelKey);
							if(propLabelObj != null) {
								if(!String.class.isAssignableFrom(propLabelObj.getClass())) {
									logger.error("{0} 的值必须是字符串类型", propLabelKey);
									hasErrors = true;
								} else {
									if(hasMaxLengthError(propLabelKey, propLabelObj.toString(), 64)) {
										hasErrors = true;
									}
								}
							}
							
							// valueType
							boolean valueTypeHasError = false;
							Object propValueTypeObj = propertyMap.get("valueType");
							if(propValueTypeObj == null || !String.class.isAssignableFrom(propValueTypeObj.getClass())) {
								logger.error("valueType 的值必须是字符串类型");
								hasErrors = true;
								valueTypeHasError = true;
							} else {
								if(!valueTypes.contains(propValueTypeObj)) {
									logger.error("valueType 的值只能是 {0}，不支持 {1}", String.join("、", valueTypes), propertiesObj);
									hasErrors = true;
									valueTypeHasError = true;
								}
							}
							
							// defaulValue
							// 默认值的类型要与 valueType 声明的保持一致
							if(!valueTypeHasError) {
								String propDefaultValueKey = "defaultValue";
								Object propDefaultValueObj = propertyMap.get(propDefaultValueKey);
								if(propDefaultValueObj != null) {
									if("string".equals(propValueTypeObj)) {
										if(!String.class.isAssignableFrom(propDefaultValueObj.getClass())) {
											logger.error("当 valueType 为 string 时，{0} 的值必须是字符串类型", propDefaultValueKey);
											hasErrors = true;
										} else {
											if(hasMaxLengthError(propDefaultValueKey, propDefaultValueObj.toString(), 32)) {
												hasErrors = true;
											}
										}
									}else if("boolean".equals(propValueTypeObj) && !Boolean.class.isAssignableFrom(propDefaultValueObj.getClass())) {
										logger.error("当 valueType 为 boolean 时，defaultValue 的值必须是布尔类型");
										hasErrors = true;
									}else if("number".equals(propValueTypeObj) && !Number.class.isAssignableFrom(propDefaultValueObj.getClass())) {
										logger.error("当 valueType 为 number 时，defaultValue 的值必须是数字类型");
										hasErrors = true;
									}
								}
							} else {
								// do nothing
							}
							
							// description
							String propDescriptionKey = "description";
							Object propDescriptionObj = propertyMap.get(propDescriptionKey);
							if(propDescriptionObj != null) {
								if(!String.class.isAssignableFrom(propDescriptionObj.getClass())) {
									logger.error("{0} 的值必须是字符串类型", propDescriptionKey);
									hasErrors = true;
								} else {
									if(hasMaxLengthError(propDescriptionKey, propDescriptionObj.toString(), 512)) {
										hasErrors = true;
									}
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
											String propOptionValueKey = "value";
											Object propOptionValueObj = propOptionMap.get(propOptionValueKey);
											if(propOptionValueObj == null || !String.class.isAssignableFrom(propOptionValueObj.getClass())) {
												logger.error("{0} 的值不能为空，且必须是字符串类型", propOptionValueKey);
												hasErrors = true;
											} else {
												if(hasMaxLengthError(propOptionValueKey, propOptionValueObj.toString(), 32)) {
													hasErrors = true;
												}
											}
											
											// label
											String propOptionLabelKey = "label";
											Object propOptionLabelObj = propOptionMap.get(propOptionLabelKey);
											if(propOptionLabelObj == null || !String.class.isAssignableFrom(propOptionLabelObj.getClass())) {
												logger.error("{0} 的值不能为空，且必须是字符串类型", propOptionLabelKey);
												hasErrors = true;
											} else {
												if(hasMaxLengthError(propOptionLabelKey, propOptionLabelObj.toString(), 32)) {
													hasErrors = true;
												}
											}
											
											// description
											String propOptionDescriptionKey = "description";
											Object propOptionDescriptionObj = propOptionMap.get(propOptionDescriptionKey);
											if(propOptionDescriptionObj != null) {
												if(!String.class.isAssignableFrom(propOptionDescriptionObj.getClass())) {
													logger.error("{0} 的值必须是字符串类型", propOptionDescriptionKey);
													hasErrors = true;
												} else {
													if(hasMaxLengthError(propOptionDescriptionKey, propOptionDescriptionObj.toString(), 512)) {
														hasErrors = true;
													}
												}
											}
											
											// TODO: 此字段未存入数据库，当做到界面设计时，如果用不到就删除
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
							String eventNameKey = "name";
							Object eventNameObj = eventMap.get(eventNameKey);
							if(eventNameObj == null || !String.class.isAssignableFrom(eventNameObj.getClass())) {
								logger.error("{0} 的值不能为空，且必须是字符串类型", eventNameKey);
								hasErrors = true;
							} else {
								if(hasMaxLengthError(eventNameKey, eventNameObj.toString(), 32)) {
									hasErrors = true;
								}
							}
							
							// label
							String eventLabelKey = "label";
							Object eventLabelObj = eventMap.get(eventLabelKey);
							if(eventLabelObj == null || !String.class.isAssignableFrom(eventLabelObj.getClass())) {
								logger.error("{0} 的值不能为空，且必须是字符串类型", eventLabelKey);
								hasErrors = true;
							} else {
								if(hasMaxLengthError(eventLabelKey, eventLabelObj.toString(), 32)) {
									hasErrors = true;
								}
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
							String eventDescriptionKey = "description";
							Object eventDescriptionObj = eventMap.get(eventDescriptionKey);
							if(eventDescriptionObj != null) {
								if(!String.class.isAssignableFrom(eventDescriptionObj.getClass())) {
									logger.error("{0} 的值必须是字符串类型", eventDescriptionKey);
									hasErrors = true;
								} else {
									if(hasMaxLengthError(eventDescriptionKey, eventDescriptionObj.toString(), 512)) {
										hasErrors = true;
									}
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
												String eventArgumentNameKey = "name";
												Object eventArgumentNameObj = eventArgumentMap.get(eventArgumentNameKey);
												if(eventArgumentNameObj == null || !String.class.isAssignableFrom(eventArgumentNameObj.getClass())) {
													logger.error("{0} 的值不能为空，且必须是字符串类型", eventArgumentNameKey);
													hasErrors = true;
												} else {
													if(hasMaxLengthError(eventArgumentNameKey, eventArgumentNameObj.toString(), 32)) {
														hasErrors = true;
													}
												}
												
												// label
												String eventArgumentLabelKey = "label";
												Object eventArgumentLabelObj = eventArgumentMap.get(eventArgumentLabelKey);
												if(eventArgumentLabelObj == null || !String.class.isAssignableFrom(eventArgumentLabelObj.getClass())) {
													logger.error("{0} 的值不能为空，且必须是字符串类型", eventArgumentLabelKey);
													hasErrors = true;
												} else {
													if(hasMaxLengthError(eventArgumentLabelKey, eventArgumentLabelObj.toString(), 32)) {
														hasErrors = true;
													}
												}
												
												// valueType
												boolean eventArgumentValueTypeHasError = false;
												Object eventArgumentValueTypeObj = eventArgumentMap.get("valueType");
												if(eventArgumentValueTypeObj != null && !String.class.isAssignableFrom(eventArgumentValueTypeObj.getClass())) {
													logger.error("valueType 的值必须是字符串类型");
													hasErrors = true;
													eventArgumentValueTypeHasError = true;
												} else {
													if(!this.valueTypes.contains(eventArgumentValueTypeObj)) {
														logger.error("valueType 的值只能是 {0}，不支持 {1}", String.join("、", valueTypes), eventArgumentValueTypeObj);
														hasErrors = true;
														eventArgumentValueTypeHasError = true;
													}
												}
												
												// defaultValue
												// 默认值的类型要与 valueType 声明的保持一致
												if(!eventArgumentValueTypeHasError) {
													String eventArgumentDefaultValueKey = "defaultValue";
													Object eventArgumentDefaultValueObj = eventArgumentMap.get(eventArgumentDefaultValueKey);
													if(eventArgumentDefaultValueObj != null) {
														if("string".equals(eventArgumentValueTypeObj)) {
															if(!String.class.isAssignableFrom(eventArgumentDefaultValueObj.getClass())) {
																logger.error("当 valueType 为 string 时，{0} 的值必须是字符串类型", eventArgumentDefaultValueKey);
																hasErrors = true;
															} else {
																if(hasMaxLengthError(eventArgumentDefaultValueKey, eventArgumentDefaultValueObj.toString(), 32)) {
																	hasErrors = true;
																}
															}
														}else if("boolean".equals(eventArgumentValueTypeObj) && !Boolean.class.isAssignableFrom(eventArgumentDefaultValueObj.getClass())) {
															logger.error("当 valueType 为 boolean 时，defaultValue 的值必须是布尔类型");
															hasErrors = true;
														}else if("number".equals(eventArgumentValueTypeObj) && !Number.class.isAssignableFrom(eventArgumentDefaultValueObj.getClass())) {
															logger.error("当 valueType 为 number 时，defaultValue 的值必须是数字类型");
															hasErrors = true;
														}
													}
												}else {
													// do nothing
												}

												// description
												String eventArgumentDescriptionKey = "description";
												Object eventArgumentDescriptionObj = eventArgumentMap.get(eventArgumentDescriptionKey);
												if(eventArgumentDescriptionObj != null) {
													if(!String.class.isAssignableFrom(eventArgumentDescriptionObj.getClass())) {
														logger.error("{0} 的值必须是字符串类型", eventArgumentDescriptionKey);
														hasErrors = true;
													} else {
														if(hasMaxLengthError(eventArgumentDescriptionKey, eventArgumentDescriptionObj.toString(), 512)) {
															hasErrors = true;
														}
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
		return hasErrors;
	}

	/**
	 * 校验是否存在 value 的最大长度超过 maxLength 指定值的错误。
	 * 
	 * @param fieldKey 字段名
	 * @param value 字段的值
	 * @param maxLength 最大长度
	 * @return 如果未通过校验，即包含错误，则返回 <code>true</code>，否则返回 <code>falsetrue</code>
	 */
	private boolean hasMaxLengthError(String fieldKey, String value, int maxLength) {
		int valueLength = StringUtils.byteLength(value.toString().trim());
		
		if(valueLength > maxLength) {
			logger.error("{0} 不能超过 {1} 个字节(一个汉字占两个字节)，当前包含 {2} 个字节", fieldKey, maxLength, valueLength);
			return true;
		}
		return false;
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
				NewWidgetChange newWidgetChange = parseNewWidget(newWidgetMap);
				
				changes.add(newWidgetChange);
			}
		}
		
		changelog.setChanges(changes);
		
		return changelog;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private NewWidgetChange parseNewWidget(Map newWidgetMap) {
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
				
				String valueType = propertyMap.get("valueType").toString();
				widgetProperty.setValueType(valueType);
				
				Object propertyDefaultValue = propertyMap.get("defaultValue");
				if(propertyDefaultValue != null) {
					widgetProperty.setDefaultValue(propertyDefaultValue);
				}
				
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
						
						argument.setValueType(argumentMap.get("valueType").toString());
						
						Object argumentDefaultValueObj = argumentMap.get("defaultValue");
						if(argumentDefaultValueObj != null) {
							argument.setDefaultValue(argumentDefaultValueObj);
						}

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
		return newWidgetChange;
	}
}
