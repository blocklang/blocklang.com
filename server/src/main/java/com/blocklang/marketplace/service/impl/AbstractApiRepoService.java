package com.blocklang.marketplace.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.util.GitUrlSegment;
import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.apiobject.ApiObject;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.model.ApiRepo;

public class AbstractApiRepoService {
	
	@Autowired
	private ApiRepoDao apiRepoDao;
	
	public <T extends ApiObject> ApiRepo saveApoRepo(RefData<T> refData) {
		Optional<ApiRepo> optional = apiRepoDao.findByGitRepoUrlAndCreateUserId(refData.getGitUrl(), refData.getCreateUserId());
		if(optional.isPresent()) {
			return optional.get();
		}
		
		String gitUrl = refData.getGitUrl();
		GitUrlSegment urlSegment = GitUrlSegment.of(gitUrl);
		
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl(refData.getGitUrl());
		apiRepo.setGitRepoWebsite(urlSegment.getWebsite());
		apiRepo.setGitRepoOwner(urlSegment.getOwner());
		apiRepo.setGitRepoName(urlSegment.getRepoName());
		apiRepo.setCategory(RepoCategory.fromValue(refData.getRepoConfig().getCategory()));
		apiRepo.setCreateTime(LocalDateTime.now());
		apiRepo.setCreateUserId(refData.getCreateUserId());
		
		return apiRepoDao.save(apiRepo);
	}
}
