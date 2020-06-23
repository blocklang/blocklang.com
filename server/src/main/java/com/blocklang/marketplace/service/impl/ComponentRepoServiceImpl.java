package com.blocklang.marketplace.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.UserService;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.blocklang.marketplace.service.ComponentRepoService;

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
	
	// Page<ComponentRepo> findAllByGitRepoNameContainingIgnoreCase(String queryForName, Pageable page);
	@Override
	public Page<ComponentRepoInfo> findAllByGitRepoNameAndExcludeStd(String queryGitRepoName, Pageable page) {
		Specification<ComponentRepo> spec = createSpecification(queryGitRepoName);
		
		return componentRepoDao.findAll(spec, page).map(componentRepo -> {
			userService.findById(componentRepo.getCreateUserId()).ifPresent(user -> {
				componentRepo.setCreateUserName(user.getLoginName());
				componentRepo.setCreateUserAvatarUrl(user.getAvatarUrl());
			});
			// 为了支持调整 API
			// 以 master 中的 API 为准
			ApiRepo apiRepo = getApiRepo(componentRepo, "master");
			return new ComponentRepoInfo(componentRepo, apiRepo);
		});
	}

	private Specification<ComponentRepo> createSpecification(String queryGitRepoName) {
		List<String> stdRepos = getStdGitRepo();

		Specification<ComponentRepo> spec = new Specification<ComponentRepo>() {
			private static final long serialVersionUID = 7373138023698470751L;

			@Override
			public Predicate toPredicate(Root<ComponentRepo> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				Predicate result = criteriaBuilder.conjunction();
				
				if(!StringUtils.isBlank(queryGitRepoName)) {
					result = criteriaBuilder.and(result, criteriaBuilder.like(
							criteriaBuilder.lower(root.get("gitRepoName")), 
							"%" + queryGitRepoName.toLowerCase() + "%"));
				}
				if(!stdRepos.isEmpty()) {
					In<String> in = criteriaBuilder.in(root.get("gitRepoUrl"));
					stdRepos.forEach(repoUrl -> in.value(repoUrl));
					result = criteriaBuilder.and(result, in.not());
				}
				
				return result;
			}
		};
		return spec;
	}

	/**
	 * 获取标准库
	 * 
	 * @return 标准库的 gitUrl 列表
	 */
	private List<String> getStdGitRepo() {
		String stdWidgetApiGitUrl = propertyService.findStringValue(CmPropKey.STD_WIDGET_API_GIT_URL, null);
		String stdWidgetIdeGitUrl = propertyService.findStringValue(CmPropKey.STD_WIDGET_IDE_GIT_URL, null);
		String stdWidgetProdDojoGitUrl = propertyService.findStringValue(CmPropKey.STD_WIDGET_BUILD_DOJO_GIT_URL, null);
		
		return Arrays.asList(stdWidgetApiGitUrl, stdWidgetIdeGitUrl, stdWidgetProdDojoGitUrl)
				.stream()
				.filter(repoUrl -> repoUrl != null)
				.collect(Collectors.toList());
	}

	private ApiRepo getApiRepo(ComponentRepo componentRepo, String version) {
		ComponentRepoVersion componentRepoVersion = componentRepoVersionDao.findByComponentRepoIdAndVersion(componentRepo.getId(), version).orElse(null);
		if (componentRepoVersion == null) {
			return null;
		}
		
		ApiRepoVersion apiRepoVersion = apiRepoVersionDao.findById(componentRepoVersion.getApiRepoVersionId()).orElse(null);
		if (apiRepoVersion == null) {
			return null;
		}
		return apiRepoDao.findById(apiRepoVersion.getApiRepoId()).orElse(null);
	}

	@Override
	public List<ComponentRepoInfo> findUserComponentRepos(Integer userId) {
		List<ComponentRepo> repos = componentRepoDao.findAllByCreateUserIdOrderByGitRepoName(userId);
		return repos.stream().map(componentRepo -> {
			ApiRepo apiRepo = getApiRepo(componentRepo, "master");
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
