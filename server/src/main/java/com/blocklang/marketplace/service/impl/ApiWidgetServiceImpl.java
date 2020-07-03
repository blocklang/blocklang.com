package com.blocklang.marketplace.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ApiWidgetDao;
import com.blocklang.marketplace.model.ApiWidget;
import com.blocklang.marketplace.service.ApiWidgetService;

/**
 * 管理 Api UI 组件的业务逻辑实现类
 * 
 * @author Zhengwei Jin
 *
 */
@Service
public class ApiWidgetServiceImpl implements ApiWidgetService {

	@Autowired
	private ApiWidgetDao apiWidgetDao;
	
	@Override
	public Optional<ApiWidget> findByApiRepoVersionIdAndNameIgnoreCase(Integer apiRepoVersionId, String widgetName) {
		return apiWidgetDao.findByApiRepoVersionIdAndNameIgnoreCase(apiRepoVersionId, widgetName);
	}

}
