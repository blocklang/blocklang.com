package com.blocklang.marketplace.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.blocklang.marketplace.model.ComponentRepo;

public interface ComponentRepoRegistryService {

	/**
	 * 只查找已发布的组件库，如果组件库已登记，但是没有发布，则不返回。
	 * 
	 * @param query
	 * @param page
	 * @return
	 */
	Page<ComponentRepo> findAllByNameOrLabel(String query, Pageable page);

}
