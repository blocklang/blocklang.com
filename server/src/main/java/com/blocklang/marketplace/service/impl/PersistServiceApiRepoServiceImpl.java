package com.blocklang.marketplace.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.apirepo.apiobject.ApiObject;
import com.blocklang.marketplace.apirepo.apiobject.service.data.ServiceData;
import com.blocklang.marketplace.service.PersistApiRefService;
import com.blocklang.marketplace.service.PersistServiceApiRepoService;
import com.blocklang.marketplace.service.ServiceApiRefService;

@Service
public class PersistServiceApiRepoServiceImpl extends AbstractApiRepoService implements PersistServiceApiRepoService {

	@Autowired
	private ServiceApiRefService serviceApiRefService;

	@Override
	public PersistApiRefService getPersistApiRefService() {
		return serviceApiRefService;
	}

	@Override
	public Class<? extends ApiObject> getApiObjectClass() {
		return ServiceData.class;
	}
	
}
