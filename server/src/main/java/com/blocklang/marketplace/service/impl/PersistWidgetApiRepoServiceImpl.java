package com.blocklang.marketplace.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.service.PersistApiRefService;
import com.blocklang.marketplace.service.PersistWidgetApiRepoService;
import com.blocklang.marketplace.service.WidgetApiRefService;

@Service
public class PersistWidgetApiRepoServiceImpl extends AbstractApiRepoService implements PersistWidgetApiRepoService {

	@Autowired
	private WidgetApiRefService persistWidgetApiRefService;

	@Override
	public PersistApiRefService getPersistApiRefService() {
		return persistWidgetApiRefService;
	}

}
