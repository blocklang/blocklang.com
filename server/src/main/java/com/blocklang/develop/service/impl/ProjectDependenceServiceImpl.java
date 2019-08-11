package com.blocklang.develop.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.dao.ProjectBuildProfileDao;
import com.blocklang.develop.dao.ProjectDependenceDao;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.model.ProjectBuildProfile;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

import de.skuzzle.semantic.Version;

@Service
public class ProjectDependenceServiceImpl implements ProjectDependenceService{
	
	@Autowired
	private ProjectDependenceDao projectDependenceDao;
	@Autowired
	private ProjectBuildProfileDao projectBuildProfileDao;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	
	@Override
	public Boolean devDependenceExists(Integer projectId, Integer componentRepoId) {
		List<ComponentRepoVersion> componentRepoVersions = componentRepoVersionDao.findAllByComponentRepoId(componentRepoId);
		if(componentRepoVersions.isEmpty()) {
			return false;
		}
		List<ProjectDependence> devDependences = projectDependenceDao.findAllByProjectIdAndProfileId(projectId, null);
		if(devDependences.isEmpty()) {
			return false;
		}
		
		return componentRepoVersions.stream().anyMatch(version -> {
			return devDependences.stream().anyMatch(dependence -> {
				return version.getId().equals(dependence.getComponentRepoVersionId());
			});
		});
	}
	
	@Override
	public Boolean buildDependenceExists(Integer projectId, Integer componentRepoId, AppType appType, String profileName) {
		List<ComponentRepoVersion> componentRepoVersions = componentRepoVersionDao.findAllByComponentRepoId(componentRepoId);
		if(componentRepoVersions.isEmpty()) {
			return false;
		}
		// 获取 profile 信息
		Optional<ProjectBuildProfile> buildProfileOption = projectBuildProfileDao.findByProjectIdAndAppTypeAndNameIgnoreCase(projectId, appType, profileName);
		if(buildProfileOption.isEmpty()) {
			return false;
		}
		List<ProjectDependence> devDependences = projectDependenceDao.findAllByProjectIdAndProfileId(projectId, buildProfileOption.get().getId());
		if(devDependences.isEmpty()) {
			return false;
		}
		
		return componentRepoVersions.stream().anyMatch(version -> {
			return devDependences.stream().anyMatch(dependence -> {
				return version.getId().equals(dependence.getComponentRepoVersionId());
			});
		});
	}

	@Transactional
	@Override
	public ProjectDependence save(Integer projectId, ComponentRepo componentRepo,  Integer createUserId) {
		// 获取组件库的最新版本信息
		List<ComponentRepoVersion> componentRepoVersions = componentRepoVersionDao.findAllByComponentRepoId(componentRepo.getId());
		if(componentRepoVersions.isEmpty()) {
			return null;
		}
		componentRepoVersions.sort(new Comparator<ComponentRepoVersion>() {
			@Override
			public int compare(ComponentRepoVersion version1, ComponentRepoVersion version2) {
				return Version.compare(Version.parseVersion(version2.getVersion()), Version.parseVersion(version1.getVersion()));
			}
		});
		Integer latestRepoVersionId = componentRepoVersions.get(0).getId();
		// 为项目生成一个默认的 Profile（如果已存在，则不生成）
		ProjectBuildProfile profile = projectBuildProfileDao
			.findByProjectIdAndAppTypeAndNameIgnoreCase(projectId, componentRepo.getAppType(), ProjectBuildProfile.DEFAULT_PROFILE_NAME)
			.orElseGet(() -> {
				ProjectBuildProfile p = new ProjectBuildProfile();
				p.setProjectId(projectId);
				p.setAppType(componentRepo.getAppType());
				p.setName(ProjectBuildProfile.DEFAULT_PROFILE_NAME);
				p.setCreateUserId(createUserId);
				p.setCreateTime(LocalDateTime.now());
				return projectBuildProfileDao.save(p);
			});

		// 为项目添加一个依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(latestRepoVersionId);
		dependence.setProfileId(profile.getId());
		dependence.setCreateUserId(createUserId);
		dependence.setCreateTime(LocalDateTime.now());
		return projectDependenceDao.save(dependence);
	}

	@Override
	public List<ProjectDependenceData> findProjectDependences(Integer projectId) {
		List<ProjectDependence> dependences = projectDependenceDao.findAllByProjectId(projectId);
		return dependences.stream().map(dependence -> {
			ComponentRepoVersion componentRepoVersion = null;
			ComponentRepo componentRepo = null;
			ApiRepo apiRepo = null;
			ApiRepoVersion apiRepoVersion = null;
			
			componentRepoVersion = componentRepoVersionDao.findById(dependence.getComponentRepoVersionId()).orElse(null);
			if(componentRepoVersion != null) {
				componentRepo = componentRepoDao.findById(componentRepoVersion.getComponentRepoId()).orElse(null);
				apiRepoVersion = apiRepoVersionDao.findById(componentRepoVersion.getApiRepoVersionId()).orElse(null);
			}
			if(apiRepoVersion != null) {
				apiRepo = apiRepoDao.findById(apiRepoVersion.getApiRepoId()).orElse(null);
			}
			
			ProjectDependenceData data = new ProjectDependenceData(dependence.getId(), componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
			return data;
		}).collect(Collectors.toList());
	}

	@Override
	public void delete(Integer dependenceId) {
		projectDependenceDao.deleteById(dependenceId);
	}

}
