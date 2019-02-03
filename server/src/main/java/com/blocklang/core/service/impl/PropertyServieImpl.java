package com.blocklang.core.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.blocklang.core.dao.PropertyDao;
import com.blocklang.core.model.CmProperty;
import com.blocklang.core.service.PropertyService;

@Service
public class PropertyServieImpl implements PropertyService {

	private static final int ROOT_ID = -1;
	
	@Autowired
	public PropertyDao propertyDao;
	
	@Override
	@Cacheable(value = "cm_properties")
	public Optional<String> findStringValue(String key) {
		Optional<CmProperty> property = propertyDao.findByKeyAndParentIdAndValid(key, ROOT_ID, true);
		return property.map(CmProperty::getValue);
	}

}
