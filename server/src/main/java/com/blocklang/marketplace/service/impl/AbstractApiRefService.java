package com.blocklang.marketplace.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.apiobject.ApiObject;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;

public abstract class AbstractApiRefService {

	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	
	public boolean isPublished(String gitUrl, Integer createUserId, String shortRefName) {
		Optional<ApiRepo> apiRepoOption = apiRepoDao.findByGitRepoUrlAndCreateUserId(gitUrl, createUserId);
		if(apiRepoOption.isEmpty()) {
			return false;
		}
		return apiRepoVersionDao.findByApiRepoIdAndVersion(apiRepoOption.get().getId(), shortRefName).isPresent();
	}

	protected <T extends ApiObject> ApiRepoVersion saveApiRepoVersion(Integer apiRepoId, RefData<T> refData) {
		ApiRepoVersion version = apiRepoVersionDao.findByApiRepoIdAndVersion(apiRepoId, refData.getShortRefName()).orElse(new ApiRepoVersion());
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
