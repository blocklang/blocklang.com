package com.blocklang.develop.dao;

import java.util.List;

import com.blocklang.develop.model.PageDataItem;

public interface PageDataJdbcDao {

	void delete(Integer pageId);

	void batchSave(Integer pageId, List<PageDataItem> allData);

}
