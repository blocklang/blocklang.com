package com.blocklang.marketplace.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.service.ApiRepoVersionService;

@Service
public class ApiRepoVersionServiceImpl implements ApiRepoVersionService {

	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	
	@Override
	public Optional<ApiRepoVersion> findById(Integer apiRepoVersionId) {
		return apiRepoVersionDao.findById(apiRepoVersionId);
	}

}
