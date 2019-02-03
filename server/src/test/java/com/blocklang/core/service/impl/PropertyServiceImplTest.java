package com.blocklang.core.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.constant.DataType;
import com.blocklang.core.dao.PropertyDao;
import com.blocklang.core.model.CmProperty;
import com.blocklang.core.service.AbstractServiceTest;
import com.blocklang.core.service.PropertyService;

public class PropertyServiceImplTest extends AbstractServiceTest{

	@Autowired
	private PropertyService propertyService;
	
	@Autowired
	private PropertyDao propertyDao;
	
	@Test
	public void find_string_value_no_data() {
		Optional<String> valueOption = propertyService.findStringValue("not-exist-key");
		assertThat(valueOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_string_value_is_invalid() {
		CmProperty property = new CmProperty();
		property.setKey("key");
		property.setValue("value");
		property.setCreateTime(LocalDateTime.now());
		property.setCreateUserId(1);
		property.setValid(false);
		property.setDataType(DataType.STRING);
		propertyDao.save(property);
		
		Optional<String> valueOption = propertyService.findStringValue("key");
		assertThat(valueOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_string_value_set_wrong_parent_id() {
		CmProperty property = new CmProperty();
		property.setKey("key");
		property.setValue("value");
		property.setCreateTime(LocalDateTime.now());
		property.setCreateUserId(1);
		property.setValid(true);
		property.setDataType(DataType.STRING);
		property.setParentId(-2); // not -1
		propertyDao.save(property);
		
		Optional<String> valueOption = propertyService.findStringValue("key");
		assertThat(valueOption.isEmpty(), is(true));
	}

	@Test(expected = ConstraintViolationException.class)
	public void find_string_value_not_set_parent_id() {
		CmProperty property = new CmProperty();
		property.setKey("key");
		property.setValue("value");
		property.setCreateTime(LocalDateTime.now());
		property.setCreateUserId(1);
		property.setValid(true);
		property.setDataType(DataType.STRING);
		property.setParentId(null); // 不能设置为 null
		propertyDao.save(property);
	}
	
	@Test
	public void find_string_value_success() {
		CmProperty property = new CmProperty();
		property.setKey("key");
		property.setValue("value");
		property.setCreateTime(LocalDateTime.now());
		property.setCreateUserId(1);
		property.setDataType(DataType.STRING);
		propertyDao.save(property);
		
		Optional<String> valueOption = propertyService.findStringValue("key");
		assertThat(valueOption.get(), equalTo("value"));
	}
	
}
