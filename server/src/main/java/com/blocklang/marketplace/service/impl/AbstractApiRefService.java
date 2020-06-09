package com.blocklang.marketplace.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.util.GitUrlSegment;
import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;

public abstract class AbstractApiRefService {

	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	
	protected <T> ApiRepo saveApoRepo(RefData<T> refData) {
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
	
	protected <T> ApiRepoVersion saveApiRepoVersion(Integer apiRepoId, RefData<T> refData) {
		ApiRepoVersion version = new ApiRepoVersion();
		version.setApiRepoId(apiRepoId);
		version.setVersion(refData.getShortRefName());
		version.setGitTagName(refData.getFullRefName());
		version.setName(refData.getRepoConfig().getName());
		version.setDisplayName(refData.getRepoConfig().getDisplayName());
		version.setDescription(refData.getRepoConfig().getDescription());
		version.setLastPublishTime(LocalDateTime.now());
		version.setCreateUserId(refData.getCreateUserId());
		version.setCreateTime(LocalDateTime.now());
		
		return apiRepoVersionDao.save(version);
	}
	
}
