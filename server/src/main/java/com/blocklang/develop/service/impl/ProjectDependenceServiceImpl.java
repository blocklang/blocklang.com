package com.blocklang.develop.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.service.PropertyService;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.dao.ProjectBuildProfileDao;
import com.blocklang.develop.dao.ProjectDao;
import com.blocklang.develop.dao.ProjectDependenceDao;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectBuildProfile;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.skuzzle.semantic.Version;

@Service
public class ProjectDependenceServiceImpl implements ProjectDependenceService{
	
	private static final Logger logger = LoggerFactory.getLogger(ProjectDependenceServiceImpl.class);
	
	@Autowired
	private ProjectDao projectDao;
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
	@Autowired
	private PropertyService propertyService;
	
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
		ProjectDependence savedDependence = projectDependenceDao.save(dependence);
		
		// 在 git 仓库中更新 DEPENDENCE.json 文件
		updateProjectDependenceFile(projectId);
		return savedDependence;
	}

	
	//	```json
	//	{
	//	    "dev": {
	//	        "web":{
	//	            "github/@publisher1/repoName1": {"git": "", "tag": "v0.1.0"},
	//	            "gitee/@publisher2/repoName2": {"git": "", "tag": "v0.1.0"}
	//	        }
	//	    },
	//	    "build": {
	//	        "web":{
	//	            "default": {
	//	                "github/@publisher1/repoName1": {"git": "", "tag": "v0.1.0"},
	//	                "github/@publisher2/repoName2": {"git": "", "tag": "v0.1.0"}
	//	            },
	//	            "profile2": {
	//	                "github/@publisher11/repoName11": {"gait": "", "tag": "v0.1.0"},
	//	                "github/@publisher22/repoName22": {"git": "", "tag": "v0.1.0"}
	//	            }
	//	        },
	//	        "wechatMiniApp": {
	//	            "default": {
	//	                "github/@publisher3/repoName3": {"git": "", "tag": "v0.1.0"},
	//	                "github/@publisher4/repoName4": {"git": "", "tag": "v0.1.0"}
	//	            }
	//	        }
	//	    }
	//	}
	//	```
	private void updateProjectDependenceFile(Integer projectId) {
		Optional<Project> projectOption = projectDao.findById(projectId);
		if(projectOption.isEmpty()) {
			return;
		}
		Project project = projectOption.get();
		Map<String, Object> result = new HashMap<String, Object>();
		// 获取项目的所有依赖
		List<ProjectDependenceData> dependences = findProjectDependences(projectId);
		// 补充 profile 信息
		for(ProjectDependenceData data : dependences) {
			// 先获取依赖信息
			ProjectDependence dependence = data.getDependence();
			// 然后根据依赖信息获取 profile 信息
			if(dependence.getProfileId() != null) {
				projectBuildProfileDao.findById(dependence.getProfileId()).ifPresent(profile -> data.setProfile(profile));
			}
		}
		
		List<ProjectDependenceData> devDependences = dependences
				.stream()
				.filter(dependence -> dependence.getComponentRepo().getIsIdeExtension())
				.collect(Collectors.toList());
		Map<String, List<ProjectDependenceData>> groupedDevDependences = devDependences
				.stream()
				.collect(Collectors.groupingBy(dependenceData -> dependenceData.getComponentRepo().getAppType().getValue()));
		
		Map<String, Object> devMap = new HashMap<String, Object>();
		for(Map.Entry<String, List<ProjectDependenceData>> entry : groupedDevDependences.entrySet()) {
			String appTypeValue = entry.getKey();
			Map<String, Object> appTypeMap = new HashMap<String, Object>();
			for(ProjectDependenceData data : entry.getValue()) {
				ComponentRepo componentRepo = data.getComponentRepo();
				String dependenceKey = componentRepo.getGitRepoWebsite() 
						+ "/" 
						+ componentRepo.getGitRepoOwner() 
						+ "/"
						+ componentRepo.getGitRepoName();

				// 当前仅支持 git
				Map<String, String> git = new HashMap<String, String>();
				git.put("git", data.getComponentRepo().getGitRepoUrl());
				git.put("tag", data.getComponentRepoVersion().getGitTagName());
				appTypeMap.put(dependenceKey, git);
			}
			devMap.put(appTypeValue, appTypeMap);
		}
		
		List<ProjectDependenceData> buildDependence = dependences
				.stream()
				.filter(dependence -> !dependence.getComponentRepo().getIsIdeExtension())
				.collect(Collectors.toList());
		Map<String, Map<String, List<ProjectDependenceData>>> groupedBuildDependences = buildDependence
				.stream()
				.collect(Collectors.groupingBy(dependenceData -> dependenceData.getComponentRepo().getAppType().getValue(), 
								Collectors.groupingBy(dependenceData -> dependenceData.getProfile().getName())));

		Map<String, Object> buildMap = new HashMap<String, Object>();
		for(Map.Entry<String, Map<String, List<ProjectDependenceData>>> groupedByAppType : groupedBuildDependences.entrySet()) {
			String appTypeValue = groupedByAppType.getKey();
			Map<String, Object> appTypeMap = new HashMap<String, Object>();
			for(Map.Entry<String, List<ProjectDependenceData>> groupedByProfile : groupedByAppType.getValue().entrySet()) {
				String profileValue = groupedByProfile.getKey();
				Map<String, Object> profileMap = new HashMap<String, Object>();
				for(ProjectDependenceData data : groupedByProfile.getValue()) {
					ComponentRepo componentRepo = data.getComponentRepo();
					String dependenceKey = componentRepo.getGitRepoWebsite() 
							+ "/" 
							+ componentRepo.getGitRepoOwner()
							+ "/" 
							+ componentRepo.getGitRepoName();
					
					Map<String, Object> dependenceMap = new HashMap<String, Object>();
					dependenceMap.put("git", componentRepo.getGitRepoUrl());
					dependenceMap.put("tag", data.getComponentRepoVersion().getGitTagName());
					
					profileMap.put(dependenceKey, dependenceMap);
				}
				
				appTypeMap.put(profileValue, profileMap);
			}
			buildMap.put(appTypeValue, appTypeMap);
		}
		
		if(!devMap.isEmpty()) {
			result.put("dev", devMap);
		}
		if(!buildMap.isEmpty()) {
			result.put("build", buildMap);
		}
		
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).map(rootDir -> {
			return new ProjectContext(project.getCreateUserName(), project.getName(), rootDir).getGitRepositoryDirectory();
		}).ifPresent(rootPath -> {
			Path path = rootPath.resolve(ProjectResource.DEPENDENCE_NAME);
			
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				String jsonContent = objectMapper.writeValueAsString(result);
				Files.writeString(path, jsonContent, StandardOpenOption.CREATE);
			} catch (IOException e) {
				logger.error("往 " + ProjectResource.DEPENDENCE_NAME + " 文件写入内容时出错", e);
			}
		});
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
			
			ProjectDependenceData data = new ProjectDependenceData(dependence, componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
			return data;
		}).collect(Collectors.toList());
	}

	@Override
	public void delete(Integer dependenceId) {
		projectDependenceDao.findById(dependenceId).ifPresent(dependence -> {
			projectDependenceDao.delete(dependence);
			updateProjectDependenceFile(dependence.getProjectId());
		});
	}

	@Override
	public ProjectDependence update(ProjectDependence dependence) {
		return projectDependenceDao.save(dependence);
	}

	@Override
	public Optional<ProjectDependence> findById(Integer dependenceId) {
		return projectDependenceDao.findById(dependenceId);
	}

}
