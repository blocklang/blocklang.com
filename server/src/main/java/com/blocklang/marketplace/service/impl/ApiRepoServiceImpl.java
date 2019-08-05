package com.blocklang.marketplace.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.service.ApiRepoService;

@Service
public class ApiRepoServiceImpl implements ApiRepoService{

	@Autowired
	private ApiRepoDao apiRepoDao;
	
	@Override
	public Optional<ApiRepo> findById(Integer apiRepoId) {
		return apiRepoDao.findById(apiRepoId);
	}

}
