package com.blocklang.marketplace.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.blocklang.core.service.UserService;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.service.ComponentRepoService;

@Service
public class ComponentRepoServiceImpl implements ComponentRepoService {

	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private UserService userService;
	
	@Override
	public Page<ComponentRepoInfo> findAllByNameOrLabel(String query, Pageable page) {
		Page<ComponentRepo> repos = null;
		if(StringUtils.isBlank(query)) {
			repos = componentRepoDao.findAll(page);
		}
		repos = componentRepoDao.findAllByNameContainingIgnoreCaseOrLabelContainingIgnoreCase(query, query, page);
		
		return repos.map(componentRepo -> {
			userService.findById(componentRepo.getCreateUserId()).ifPresent(user -> {
				componentRepo.setCreateUserName(user.getLoginName());
				componentRepo.setCreateUserAvatarUrl(user.getAvatarUrl());
			});
			ApiRepo apiRepo = apiRepoDao.findById(componentRepo.getApiRepoId()).orElse(null);
			return new ComponentRepoInfo(componentRepo, apiRepo);
		});
	}

	@Override
	public List<ComponentRepoInfo> findUserComponentRepos(Integer userId) {
		List<ComponentRepo> repos = componentRepoDao.findAllByCreateUserIdOrderByName(userId);
		return repos.stream().map(componentRepo -> {
			ApiRepo apiRepo = apiRepoDao.findById(componentRepo.getApiRepoId()).orElse(null);
			return new ComponentRepoInfo(componentRepo, apiRepo);
		}).collect(Collectors.toList());
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
