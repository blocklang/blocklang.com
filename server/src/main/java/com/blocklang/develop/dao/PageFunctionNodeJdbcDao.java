package com.blocklang.develop.dao;

import java.util.List;

import com.blocklang.develop.model.PageFunctionNode;

public interface PageFunctionNodeJdbcDao {

	void batchSave(List<PageFunctionNode> nodes);

	void deleteByPageId(Integer pageId);

}
