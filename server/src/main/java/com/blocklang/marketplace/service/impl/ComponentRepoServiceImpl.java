package com.blocklang.marketplace.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.UserService;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.service.ComponentRepoService;
import com.nimbusds.oauth2.sdk.util.StringUtils;

@Service
public class ComponentRepoServiceImpl implements ComponentRepoService {

	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private UserService userService;
	@Autowired
	private PropertyService propertyService;
	
	@Override
	public Page<ComponentRepoInfo> findAllByGitRepoName(String queryGitRepoName, Pageable page) {
		String version = "master";
		if(StringUtils.isBlank(queryGitRepoName)) {
			return Page.empty();
		}
		
		List<String> stdRepos = findAllStdGitRepo();
		return componentRepoDao.findAllByGitRepoNameContainingIgnoreCase(queryGitRepoName, page).map(componentRepo -> {
			userService.findById(componentRepo.getCreateUserId()).ifPresent(user -> {
				componentRepo.setCreateUserName(user.getLoginName());
				componentRepo.setCreateUserAvatarUrl(user.getAvatarUrl());
			});
			
			componentRepo.setStd(stdRepos.stream().anyMatch(gitUrl -> gitUrl.equals(componentRepo.getGitRepoUrl())));
			return getComponentRepoVersionAndApiRepoVersion(version, componentRepo, stdRepos);
		});
	}

	/**
	 * 获取所有系统内置的标准库。
	 * 
	 * @return 标准库的 gitUrl 列表，gitUrl 是一个完整的 https url。
	 */
	private List<String> findAllStdGitRepo() {
		String[] stdRepos = {CmPropKey.STD_WIDGET_API_GIT_URL, 
				CmPropKey.STD_WIDGET_IDE_GIT_URL, 
				CmPropKey.STD_WIDGET_BUILD_DOJO_GIT_URL,
				CmPropKey.STD_MINI_PROGRAM_COMPONENT_API_GIT_URL,
				CmPropKey.STD_MINI_PROGRAM_COMPONENT_IDE_GIT_URL,
				CmPropKey.STD_MINI_PROGRAM_COMPONENT_PROD_GIT_URL};
		return Arrays.stream(stdRepos)
				.map(repoKey -> propertyService.findStringValue(repoKey, null))
				.filter(repoUrl -> repoUrl != null)
				.collect(Collectors.toList());
	}

	@Override
	public List<ComponentRepoInfo> findUserComponentRepos(Integer userId) {
		var version = "master";
		List<String> stdRepos = findAllStdGitRepo();
		return componentRepoDao
				.findAllByCreateUserIdOrderByGitRepoName(userId)
				.stream()
				.map(componentRepo -> {
					componentRepo.setStd(stdRepos.stream().anyMatch(gitUrl -> gitUrl.equals(componentRepo.getGitRepoUrl())));
					return getComponentRepoVersionAndApiRepoVersion(version, componentRepo, stdRepos);
				})
				.collect(Collectors.toList());
	}

	private ComponentRepoInfo getComponentRepoVersionAndApiRepoVersion(String version, ComponentRepo componentRepo, List<String> stdRepos) {
		ComponentRepoInfo componentRepoInfo = new ComponentRepoInfo();
		componentRepoInfo.setComponentRepo(componentRepo);
		componentRepoVersionDao.findByComponentRepoIdAndVersion(componentRepo.getId(), version)
			.ifPresent(componentRepoVersion -> {
				componentRepoInfo.setComponentRepoVersion(componentRepoVersion);
				apiRepoVersionDao.findById(componentRepoVersion.getApiRepoVersionId())
					.ifPresent(apiRepoVersion -> {
						componentRepoInfo.setApiRepoVersion(apiRepoVersion);
						apiRepoDao.findById(apiRepoVersion.getApiRepoId())
							.ifPresent(apiRepo -> {
								apiRepo.setStd(stdRepos.stream().anyMatch(gitUrl -> gitUrl.equals(apiRepo.getGitRepoUrl())));
								componentRepoInfo.setApiRepo(apiRepo);
							});
					});
			});
		return componentRepoInfo;
	}

	@Override
	public boolean existsByCreateUserIdAndGitRepoUrl(Integer userId, String gitRepoUrl) {
		return componentRepoDao.existsByCreateUserIdAndGitRepoUrl(userId, gitRepoUrl);
	}

	@Override
	public Optional<ComponentRepo> findById(Integer componentRepoId) {
		return componentRepoDao.findById(componentRepoId);
	}

}
