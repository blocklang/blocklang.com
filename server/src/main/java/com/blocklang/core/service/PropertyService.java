package com.blocklang.core.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.core.model.CmProperty;

/**
 * 系统参数的业务逻辑接口
 * 
 * @author Zhengwei Jin
 *
 */
public interface PropertyService {

	/**
	 * 根据属性名获取字符串类型的属性值
	 * 
	 * @param key 属性名，不忽略大小写
	 * @return 字符串类型的属性值
	 */
	Optional<String> findStringValue(String key);
	
	/**
	 * 根据属性名获取字符串类型的属性值
	 * 
	 * <p>注意：不会缓存默认值。
	 * 
	 * @param key 属性名，不忽略大小写
	 * @param defaultValue 如果根据 key 没有找到值，则返回此默认值
	 * @return 字符串类型的属性值
	 */
	String findStringValue(String key, String defaultValue);
	
	/**
	 * 根据属性名获取数字类型的属性值
	 * @param key 属性名，不忽略大小写
	 * @return 数字类型的属性值
	 */
	Optional<Integer> findIntegerValue(String key);
	
	/**
	 * 根据属性名获取数字类型的属性值
	 * @param key 属性名，不忽略大小写
	 * @param defaultValue 如果根据 key 没有找到值，则返回此默认值
	 * @return 数字类型的属性值
	 */
	Integer findIntegerValue(String key, Integer defaultValue);

	/**
	 * 根据父属性的 key 值获取所有直属子属性
	 * 
	 * @param parentKey 父属性的 key 值
	 * @return 直属子属性列表
	 */
	List<CmProperty> findAllByParentKey(String parentKey);

}
