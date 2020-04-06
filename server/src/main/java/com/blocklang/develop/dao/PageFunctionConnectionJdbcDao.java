package com.blocklang.develop.dao;

import java.util.List;

import com.blocklang.develop.model.PageFunctionConnection;

public interface PageFunctionConnectionJdbcDao {

	void batchSave(List<PageFunctionConnection> connections);

	void deleteByPageId(Integer pageId);
}
