package com.blocklang.core.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.core.model.CmProperty;

public interface PropertyService {

	/**
	 * 根据属性名获取属性值
	 * 
	 * @param key 属性名，不忽略大小写
	 * @return 字符串类型的属性值
	 */
	Optional<String> findStringValue(String key);
	
	/**
	 * 注意：不会缓存默认值。
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	String findStringValue(String key, String defaultValue);
	
	Integer findIntegerValue(String key, Integer defaultValue);

	List<CmProperty> findAllByParentKey(String parentKey);

}
