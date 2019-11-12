package com.blocklang.core.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.dao.PropertyDao;
import com.blocklang.core.model.CmProperty;
import com.blocklang.core.service.PropertyService;

@Service
public class PropertyServieImpl implements PropertyService {
	
	@Autowired
	public PropertyDao propertyDao;
	
	@Override
	@Cacheable(value = "cm_properties")
	public Optional<String> findStringValue(String key) {
		Optional<CmProperty> property = propertyDao.findByKeyAndParentIdAndValid(key, Constant.TREE_ROOT_ID, true);
		return property.map(CmProperty::getValue);
	}

	@Override
	@Cacheable(value = "cm_properties", key = "#key")
	public String findStringValue(String key, String defaultValue) {
		return this.findStringValue(key).orElse(defaultValue);
	}

	@Override
	@Cacheable(value = "cm_properties")
	public List<CmProperty> findAllByParentKey(String parentKey) {
		Optional<CmProperty> parentPropOption = propertyDao.findByKeyAndParentIdAndValid(parentKey, Constant.TREE_ROOT_ID, true);
		if(parentPropOption.isPresent()) {
			return propertyDao.findAllByParentId(parentPropOption.get().getId());
		}
		return Collections.emptyList();
	}

	@Override
	@Cacheable(value = "cm_properties", key = "#key")
	public Integer findIntegerValue(String key, Integer defaultValue) {
		return this.findStringValue(key).map(str -> Integer.valueOf(str)).orElse(defaultValue);
	}

}
