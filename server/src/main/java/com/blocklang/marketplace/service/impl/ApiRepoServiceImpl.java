package com.blocklang.marketplace.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.service.ApiRepoService;

/**
 * Api 仓库业务逻辑实现类
 * 
 * @author Zhengwei Jin
 *
 */
@Service
public class ApiRepoServiceImpl implements ApiRepoService{

	@Autowired
	private ApiRepoDao apiRepoDao;
	
	@Override
	public Optional<ApiRepo> findById(Integer apiRepoId) {
		return apiRepoDao.findById(apiRepoId);
	}

	@Override
	public Optional<ApiRepo> findByGitUrlAndCreateUserId(String apiGitUrl, Integer userId) {
		return apiRepoDao.findByGitRepoUrlAndCreateUserId(apiGitUrl, userId);
	}

}
