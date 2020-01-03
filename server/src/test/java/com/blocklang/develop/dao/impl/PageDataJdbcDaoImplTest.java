package com.blocklang.develop.dao.impl;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractDaoTest;
import com.blocklang.develop.dao.PageDataJdbcDao;
import com.blocklang.develop.model.PageDataItem;

public class PageDataJdbcDaoImplTest extends AbstractDaoTest{

	@Autowired
	private PageDataJdbcDao pageDataJdbcDao;
	
	@Test
	public void delete_no_data() {
		pageDataJdbcDao.delete(1);
		assertThat(countRowsInTable("PAGE_DATA")).isEqualTo(0);
	}
	
	@Test
	public void batchSave() {
		Integer pageId = 1;
		
		List<PageDataItem> allData = new ArrayList<>();
		PageDataItem item1 = new PageDataItem();
		item1.setId("id");
		item1.setName("name");
		item1.setPageId(pageId);
		item1.setParentId("-1");
		item1.setType("String");
		allData.add(item1);
		
		pageDataJdbcDao.batchSave(pageId, allData);
		
		assertThat(countRowsInTable("PAGE_DATA")).isEqualTo(1);
		
		pageDataJdbcDao.delete(1);
		assertThat(countRowsInTable("PAGE_DATA")).isEqualTo(0);
	}
}
