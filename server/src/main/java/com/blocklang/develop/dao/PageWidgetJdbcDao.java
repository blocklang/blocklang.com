package com.blocklang.develop.dao;

import java.util.List;

import com.blocklang.develop.designer.data.AttachedWidget;
import com.blocklang.develop.model.PageWidgetAttrValue;

public interface PageWidgetJdbcDao {
	
	void batchSaveWidgets(Integer pageId, List<AttachedWidget> widgets);
	
	void batchSaveWidgetProperties(List<PageWidgetAttrValue> properties);
	
	void deleteWidgetProperties(Integer pageId);
	
	void deleteWidgets(Integer pageId);
}
