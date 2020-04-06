package com.blocklang.develop.dao;

import java.util.List;

import com.blocklang.develop.model.PageFunctionNodePort;

public interface PageFunctionNodePortJdbcDao {

	void batchSave(List<PageFunctionNodePort> ports);

	void deleteByPageId(Integer pageId);

}
