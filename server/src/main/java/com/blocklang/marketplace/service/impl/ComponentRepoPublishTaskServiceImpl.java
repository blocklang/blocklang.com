package com.blocklang.marketplace.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ComponentRepoPublishTaskDao;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;

@Service
public class ComponentRepoPublishTaskServiceImpl implements ComponentRepoPublishTaskService{

	@Autowired
	private ComponentRepoPublishTaskDao componentRepoPublishTaskDao;
	
	@Override
	public ComponentRepoPublishTask save(ComponentRepoPublishTask task) {
		return componentRepoPublishTaskDao.save(task);
	}

}
