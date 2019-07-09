package com.blocklang.marketplace.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.service.ComponentRepoService;

@Service
public class ComponentRepoServiceImpl implements ComponentRepoService {

	@Autowired
	private ComponentRepoDao componentRepoDao;
	
	@Override
	public Page<ComponentRepo> findAllByNameOrLabel(String query, Pageable page) {
		if(StringUtils.isBlank(query)) {
			return componentRepoDao.findAllByLastPublishTimeNotNull(page);
		}
		return componentRepoDao.findAllByLastPublishTimeNotNullAndNameContainingIgnoreCaseOrLastPublishTimeNotNullAndLabelContainingIgnoreCase(query, query, page);
	}

	@Override
	public List<ComponentRepo> findUserComponentRepos(Integer userId) {
		return componentRepoDao.findAllByCreateUserIdOrderByName(userId);
	}

}
