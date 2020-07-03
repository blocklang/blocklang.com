package com.blocklang.marketplace.service;

import java.util.Optional;

import com.blocklang.marketplace.model.ApiWidget;

/**
 * 管理 Api UI 组件的业务逻辑接口
 * 
 * @author Zhengwei Jin
 *
 */
public interface ApiWidgetService {

	/**
	 * 根据 Api 仓库的版本标识和部件名查询部件信息
	 * 
	 * @param apiRepoVersionId 根据 Api 仓库的版本标识
	 * @param widgetName 部件名
	 * @return 部件信息
	 */
	Optional<ApiWidget> findByApiRepoVersionIdAndNameIgnoreCase(Integer apiRepoVersionId, String widgetName);

}
