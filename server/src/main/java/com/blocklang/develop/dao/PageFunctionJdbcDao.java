package com.blocklang.develop.dao;

import java.util.List;

import com.blocklang.develop.model.PageFunction;

public interface PageFunctionJdbcDao {

	void batchSave(List<PageFunction> pageFunctions);

	void deleteByPageId(Integer pageId);

}
