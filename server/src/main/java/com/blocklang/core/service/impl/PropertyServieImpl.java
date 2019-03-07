package com.blocklang.core.service.impl;

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
	public String findStringValue(String key, String defaultValue) {
		return propertyDao.findByKeyAndParentIdAndValid(key, Constant.TREE_ROOT_ID, true)
				.map(CmProperty::getValue)
				.orElse(defaultValue);
	}

}
