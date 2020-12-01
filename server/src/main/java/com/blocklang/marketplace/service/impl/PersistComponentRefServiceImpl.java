package com.blocklang.marketplace.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.util.GitUrlSegment;
import com.blocklang.develop.constant.AppType;
import com.blocklang.marketplace.componentrepo.RefData;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.RepoType;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.blocklang.marketplace.service.PersistComponentRepoService;

@Service
public class PersistComponentRefServiceImpl implements PersistComponentRepoService{

	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	
	@Transactional
	@Override
	public void save(List<RefData> refDatas) {
		if(refDatas.isEmpty()) {
			return;
		}
		ComponentRepo repo = saveComponentRepo(refDatas.get(0));
		for(RefData refData : refDatas) {
			saveOrUpdateComponentVersion(repo.getId(), refData);
		}
	}

	// 如果是 tag，如果已存在，则不再修改
	// 如果是 master，如果已存在，则依然修改
	private void saveOrUpdateComponentVersion(Integer repoId, RefData refData) {
		Integer apiRepoVersionId = getApiRepoVersionId(refData);
		if(apiRepoVersionId == null) {
			return;
		}
		
		Optional<ComponentRepoVersion> repoVersionOption = componentRepoVersionDao.findByComponentRepoIdAndVersion(repoId, refData.getShortRefName());

		if(GitUtils.isDefaultBranch(refData.getShortRefName())) {
			if(repoVersionOption.isPresent()) {
				ComponentRepoVersion repoVersion = repoVersionOption.get();
				updateComponentVersion(repoVersion, refData);
			} else {
				newComponentVersion(repoId, refData, apiRepoVersionId);
			}
		} else {
			if(repoVersionOption.isEmpty()) {
				newComponentVersion(repoId, refData, apiRepoVersionId);
			}
		}
	}
	
	private void updateComponentVersion(ComponentRepoVersion repoVersion, RefData refData) {
		repoVersion.setAppType(AppType.fromValue(refData.getRepoConfig().getAppType()));
		repoVersion.setName(refData.getRepoConfig().getName());
		repoVersion.setDisplayName(refData.getRepoConfig().getDisplayName());
		repoVersion.setBuild(refData.getRepoConfig().getBuild());
		repoVersion.setDescription(refData.getRepoConfig().getDescription());
		repoVersion.setLanguage(Language.fromValue(refData.getRepoConfig().getLanguage()));
		repoVersion.setLastPublishTime(LocalDateTime.now());
		// 未设置 logo_path
		componentRepoVersionDao.save(repoVersion);
	}

	private void newComponentVersion(Integer repoId, RefData refData, Integer apiRepoVersionId) {
		ComponentRepoVersion repoVersion = new ComponentRepoVersion();
		repoVersion.setApiRepoVersionId(apiRepoVersionId);
		repoVersion.setComponentRepoId(repoId);
		repoVersion.setCreateTime(LocalDateTime.now());
		repoVersion.setCreateUserId(refData.getCreateUserId());
		repoVersion.setGitTagName(refData.getFullRefName());
		repoVersion.setVersion(refData.getShortRefName());
		repoVersion.setAppType(AppType.fromValue(refData.getRepoConfig().getAppType()));
		repoVersion.setName(refData.getRepoConfig().getName());
		repoVersion.setDisplayName(refData.getRepoConfig().getDisplayName());
		repoVersion.setBuild(refData.getRepoConfig().getBuild());
		repoVersion.setDescription(refData.getRepoConfig().getDescription());
		repoVersion.setLanguage(Language.fromValue(refData.getRepoConfig().getLanguage()));
		repoVersion.setLastPublishTime(LocalDateTime.now());
		// 未设置 logo_path
		componentRepoVersionDao.save(repoVersion);
	}

	private Integer getApiRepoVersionId(RefData refData) {
		String apiRepoGitUrl = refData.getRepoConfig().getApi().getGit();
		Optional<ApiRepo> apiRepoOption = apiRepoDao.findByGitRepoUrlAndCreateUserId(apiRepoGitUrl, refData.getCreateUserId());
		if(apiRepoOption.isEmpty()) {
			return null;
		}
		
		Integer apiRepoId = apiRepoOption.get().getId();
		Optional<ApiRepoVersion> versionOption = apiRepoVersionDao.findByApiRepoIdAndVersion(apiRepoId, refData.getShortRefName());
		if(versionOption.isEmpty()) {
			return null;
		}
		return versionOption.get().getId();
	}

	private ComponentRepo saveComponentRepo(RefData refData) {
		String gitUrl = refData.getGitUrl();
		Integer userId = refData.getCreateUserId();
		
		Optional<ComponentRepo> repoOption = componentRepoDao.findByGitRepoUrlAndCreateUserId(gitUrl, userId);
		
		if(repoOption.isPresent()) {
			ComponentRepo repo = repoOption.get();
			repo.setLastUpdateTime(LocalDateTime.now());
			repo.setLastUpdateUserId(userId);
			repo.setLastPublishTime(LocalDateTime.now());
			return componentRepoDao.save(repo);
		} else {
			GitUrlSegment segment = GitUrlSegment.of(gitUrl);
			ComponentRepo repo = new ComponentRepo();
			repo.setGitRepoUrl(refData.getGitUrl());
			repo.setGitRepoName(segment.getRepoName());
			repo.setGitRepoOwner(segment.getOwner());
			repo.setGitRepoWebsite(segment.getWebsite());
			repo.setCategory(RepoCategory.fromValue(refData.getRepoConfig().getCategory()));
			repo.setRepoType(RepoType.fromValue(refData.getRepoConfig().getRepo()));
			repo.setLastPublishTime(LocalDateTime.now());
			repo.setCreateUserId(userId);
			repo.setCreateTime(LocalDateTime.now());
			return componentRepoDao.save(repo);
		}
	}

}
