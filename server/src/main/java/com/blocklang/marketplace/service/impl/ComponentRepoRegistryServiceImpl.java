package com.blocklang.marketplace.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ComponentRepoRegistryDao;
import com.blocklang.marketplace.model.ComponentRepoRegistry;
import com.blocklang.marketplace.service.ComponentRepoRegistryService;

@Service
public class ComponentRepoRegistryServiceImpl implements ComponentRepoRegistryService {

	@Autowired
	private ComponentRepoRegistryDao componentRepoRegistryDao;
	
	@Override
	public Page<ComponentRepoRegistry> findAllByNameOrLabel(String query, Pageable page) {
		if(StringUtils.isBlank(query)) {
			return componentRepoRegistryDao.findAllByLastPublishTimeNotNull(page);
		}
		return componentRepoRegistryDao.findAllByLastPublishTimeNotNullAndNameContainingIgnoreCaseOrLastPublishTimeNotNullAndLabelContainingIgnoreCase(query, query, page);
	}

}
