package com.blocklang.marketplace.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.service.ComponentRepoRegistryService;

@Service
public class ComponentRepoRegistryServiceImpl implements ComponentRepoRegistryService {

	@Autowired
	private ComponentRepoDao componentRepoRegistryDao;
	
	@Override
	public Page<ComponentRepo> findAllByNameOrLabel(String query, Pageable page) {
		if(StringUtils.isBlank(query)) {
			return componentRepoRegistryDao.findAllByLastPublishTimeNotNull(page);
		}
		return componentRepoRegistryDao.findAllByLastPublishTimeNotNullAndNameContainingIgnoreCaseOrLastPublishTimeNotNullAndLabelContainingIgnoreCase(query, query, page);
	}

}
