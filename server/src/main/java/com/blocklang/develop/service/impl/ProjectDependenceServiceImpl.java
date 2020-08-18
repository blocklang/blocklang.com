package com.blocklang.develop.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
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
import com.blocklang.develop.dao.RepositoryDao;
import com.blocklang.develop.dao.ProjectDependenceDao;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.designer.data.ApiRepoVersionInfo;
import com.blocklang.develop.designer.data.EventArgument;
import com.blocklang.develop.designer.data.Widget;
import com.blocklang.develop.designer.data.WidgetCategory;
import com.blocklang.develop.designer.data.WidgetProperty;
import com.blocklang.develop.designer.data.RepoWidgetList;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.ProjectBuildProfile;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.marketplace.constant.WidgetPropertyValueType;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.RepoType;
import com.blocklang.marketplace.dao.ApiWidgetPropertyDao;
import com.blocklang.marketplace.dao.ApiWidgetEventArgDao;
import com.blocklang.marketplace.dao.ApiWidgetDao;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.blocklang.marketplace.service.ComponentRepoVersionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProjectDependenceServiceImpl implements ProjectDependenceService{
	
	private static final Logger logger = LoggerFactory.getLogger(ProjectDependenceServiceImpl.class);
	
	@Autowired
	private RepositoryDao projectDao;
	@Autowired
	private ProjectDependenceDao projectDependenceDao;
	@Autowired
	private ProjectBuildProfileDao projectBuildProfileDao;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ComponentRepoVersionService componentRepoVersionService;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiWidgetDao apiComponentDao;
	@Autowired
	private ApiWidgetPropertyDao apiComponentAttrDao;
	@Autowired
	private ApiWidgetEventArgDao apiComponentAttrFunArgDao;
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

//	componentRepoVersions.sort(new Comparator<ComponentRepoVersion>() {
//		@Override
//		public int compare(ComponentRepoVersion version1, ComponentRepoVersion version2) {
//			// master 排在最后
//			if(version1.getVersion().equals("master")) {
//				return 1;
//			}
//			if(version2.getVersion().equals("master")) {
//				return 1;
//			}
//			return Version.compare(Version.parseVersion(version2.getVersion()), Version.parseVersion(version1.getVersion()));
//		}
//	});
	
	@Transactional
	@Override
	public ProjectDependence save(Integer repoId, Integer projectId, ComponentRepo componentRepo, Integer createUserId) {
		// 获取组件库的最新版本信息
		List<ComponentRepoVersion> componentRepoVersions = componentRepoVersionDao.findAllByComponentRepoId(componentRepo.getId());
		if(componentRepoVersions.isEmpty()) {
			logger.error("未发现已发布的版本");
			return null;
		}
		// 默认依赖 master 分支
		Optional<ComponentRepoVersion> masterRepoVersionOption = componentRepoVersions
				.stream()
				.filter(version -> version.getVersion().equals("master"))
				.findFirst();
		if(masterRepoVersionOption.isEmpty()) {
			logger.error("未找到 master");
			return null;
		}
		ComponentRepoVersion masterRepoVersion = masterRepoVersionOption.get();
		Integer masterRepoVersionId = masterRepoVersion.getId();

		// 为项目添加一个依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(masterRepoVersionId);
		if(!RepoType.IDE.equals(componentRepo.getRepoType())) {
			// 为项目生成一个默认的 Profile（如果已存在，则不生成）
			ProjectBuildProfile profile = projectBuildProfileDao
				.findByProjectIdAndAppTypeAndNameIgnoreCase(projectId, masterRepoVersion.getAppType(), ProjectBuildProfile.DEFAULT_PROFILE_NAME)
				.orElseGet(() -> {
					ProjectBuildProfile p = new ProjectBuildProfile();
					p.setProjectId(projectId);
					p.setAppType(masterRepoVersion.getAppType());
					p.setName(ProjectBuildProfile.DEFAULT_PROFILE_NAME);
					p.setCreateUserId(createUserId);
					p.setCreateTime(LocalDateTime.now());
					return projectBuildProfileDao.save(p);
				});
			dependence.setProfileId(profile.getId());
		}
		dependence.setCreateUserId(createUserId);
		dependence.setCreateTime(LocalDateTime.now());
		ProjectDependence savedDependence = projectDependenceDao.save(dependence);
		
		// 在 git 仓库中更新 DEPENDENCE.json 文件
		updateProjectDependenceFile(repoId, projectId);
		return savedDependence;
	}

	
	//```json
	//{
	//    "dev": {
	//        "web":{
	//            "github/@publisher1/repoName1": {"git": "", "tag": "v0.1.0"},
	//            "gitee/@publisher2/repoName2": {"git": "", "tag": "v0.1.0"}
	//        }
	//    },
	//    "build": {
	//        "web":{
	//            "default": {
	//                "github/@publisher1/repoName1": {"git": "", "tag": "v0.1.0"},
	//                "github/@publisher2/repoName2": {"git": "", "tag": "v0.1.0"}
	//            },
	//            "profile2": {
	//                "github/@publisher11/repoName11": {"gait": "", "tag": "v0.1.0"},
	//                "github/@publisher22/repoName22": {"git": "", "tag": "v0.1.0"}
	//            }
	//        },
	//        "wechatMiniApp": {
	//            "default": {
	//                "github/@publisher3/repoName3": {"git": "", "tag": "v0.1.0"},
	//                "github/@publisher4/repoName4": {"git": "", "tag": "v0.1.0"}
	//            }
	//        }
	//    }
	//}
	//```
	private void updateProjectDependenceFile(Integer repoId, Integer projectId) {
		Optional<Repository> projectOption = projectDao.findById(projectId);
		if(projectOption.isEmpty()) {
			return;
		}
		Repository project = projectOption.get();
		
		// 在依赖中补充 profile 详情
		List<ProjectDependenceData> dependences = appendBuildProfile(repoId, projectId);
		
		// 转换为 DEPENDENCE.json 期望的个数
		Map<String, Object> result = convertToDependenceJsonFile(dependences);
		
		String fileName = RepositoryResource.DEPENDENCE_NAME;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
			updateGitFile(project, fileName, jsonContent);
		} catch (JsonProcessingException e) {
			logger.error("转换为 json 字符串时出错", e);
		}
	}

	private void updateGitFile(Repository project, String fileName, String content) {
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).map(rootDir -> {
			return new ProjectContext(project.getCreateUserName(), project.getName(), rootDir).getGitRepositoryDirectory();
		}).ifPresent(rootPath -> {
			Path path = rootPath.resolve(fileName);
			try {
				Files.writeString(path, content);
			} catch (IOException e) {
				logger.error("往 " + fileName + " 文件写入内容时出错", e);
			}
		});
	}

	private Map<String, Object> convertToDependenceJsonFile(List<ProjectDependenceData> dependences) {
		if(dependences.isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		List<ProjectDependenceData> devDependences = dependences
				.stream()
				.filter(dependence -> dependence.getComponentRepo().getRepoType().equals(RepoType.IDE))
				.collect(Collectors.toList());
		Map<String, List<ProjectDependenceData>> groupedDevDependences = devDependences.stream()
				.collect(Collectors.groupingBy(dependenceData -> dependenceData.getComponentRepoVersion().getAppType().getValue()));
		
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
				.filter(dependence -> !dependence.getComponentRepo().getRepoType().equals(RepoType.IDE))
				.collect(Collectors.toList());
		
		Map<String, Map<String, List<ProjectDependenceData>>> groupedBuildDependences = buildDependence
				.stream()
				.collect(Collectors.groupingBy(dependenceData -> dependenceData.getComponentRepoVersion().getAppType().getValue(), 
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
		return result;
	}

	private List<ProjectDependenceData> appendBuildProfile(Integer repoId, Integer projectId) {
		// 获取项目的所有依赖
		List<ProjectDependenceData> dependences = findProjectDependences(repoId, projectId);
		// 补充 profile 信息
		for(ProjectDependenceData data : dependences) {
			// 先获取依赖信息
			ProjectDependence dependence = data.getDependence();
			// 然后根据依赖信息获取 profile 信息
			if(dependence.getProfileId() != null) {
				projectBuildProfileDao.findById(dependence.getProfileId()).ifPresent(profile -> data.setProfile(profile));
			}
		}
		return dependences;
	}

	@Override
	public List<ProjectDependenceData> findProjectDependences(Integer repoId, Integer projectId, boolean includeStd) {
		List<ProjectDependence> dependences;
		if(includeStd) {
			dependences = this.findAllByProjectId(repoId, projectId);
		} else {
			dependences = projectDependenceDao.findAllByProjectId(projectId);
		}
		
		return convert(dependences);
	}

	private List<ProjectDependenceData> convert(List<ProjectDependence> dependences) {
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
	public List<ProjectDependenceData> findProjectDependences(Integer repoId, Integer projectId) {
		return this.findProjectDependences(repoId, projectId, false);
	}
	
	@Override
	public List<ProjectDependenceData> findProjectBuildDependences(Integer projectId) {
		List<ProjectDependence> allDeps = projectDependenceDao.findAllByProjectId(projectId);
		// FIXME: 当前仅支持 web，需要考虑支持其他类型。
		ProjectBuildProfile defaultProfile = projectBuildProfileDao.findByProjectIdAndAppTypeAndNameIgnoreCase(projectId, AppType.WEB, ProjectBuildProfile.DEFAULT_PROFILE_NAME).orElse(null);

		// 过滤出 build 版 default profile 依赖
		// 只有 build 版才有 profileId 值，dev 版 profileId 的值为 null
		List<ProjectDependence> allBuildDefaultProfileDeps = allDeps.stream()
				.filter(dep -> defaultProfile != null && defaultProfile.getId().equals(dep.getProfileId())).collect(Collectors.toList());
		// 添加标准库
		this.findProjectBuildDependenceByStandardAndDefaultProfile().ifPresent(dep -> {
			dep.setProjectId(projectId);
			allBuildDefaultProfileDeps.add(dep);
		});
		
		if(allBuildDefaultProfileDeps.isEmpty()) {
			logger.error("项目中不存在 default profile");
			return Collections.emptyList();
		}
		
		return convert(allBuildDefaultProfileDeps);
	}

	@Override
	public void delete(Integer dependenceId) {
		projectDependenceDao.findById(dependenceId).ifPresent(dependence -> {
			projectDependenceDao.delete(dependence);
			Integer repoId = 
			updateProjectDependenceFile(dependence.getRepoId(), dependence.getProjectId());
		});
	}

	@Override
	public ProjectDependence update(ProjectDependence dependence) {
		ProjectDependence savedDependence = projectDependenceDao.save(dependence);
		updateProjectDependenceFile(dependence.getProjectId());
		return savedDependence;
	}

	@Override
	public Optional<ProjectDependence> findById(Integer dependenceId) {
		return projectDependenceDao.findById(dependenceId);
	}
	
	@Override
	public List<RepoWidgetList> findAllWidgets(Integer projectId) {
		// 获取项目的所有依赖，包含组件仓库的版本信息
		List<ProjectDependence> allDependences = projectDependenceDao.findAllByProjectId(projectId);
		
		// 转换为对应的 API 仓库的版本信息
		return allDependences
			.stream()
			.flatMap(item -> componentRepoVersionDao.findById(item.getComponentRepoVersionId()).stream())
			.map(item -> item.getApiRepoVersionId())
			// 如果多个组件实现的是同一个 api repo version，则先去重
			.distinct()
			.map(apiVersionId -> {
				Optional<ApiRepo> apiRepoOption = apiRepoVersionDao
						.findById(apiVersionId)
						.flatMap(apiRepoVersion -> apiRepoDao.findById(apiRepoVersion.getApiRepoId()));
				ApiRepoVersionInfo result = new ApiRepoVersionInfo();
				result.setApiRepoVersionId(apiVersionId);
				apiRepoOption.ifPresent(apiRepo -> {
					// FIXME: 是否需要该字段名
					result.setApiRepoName(apiRepo.getGitRepoUrl());
					result.setApiRepoId(apiRepo.getId());
					result.setCategory(apiRepo.getCategory());
				});
				return result;
			})
			.filter(apiVersionInfo -> apiVersionInfo.getCategory() == RepoCategory.WIDGET)
			.map(apiVersionInfo -> {
				// 查出依赖中的所有部件
				List<Widget> widgets = apiComponentDao
						.findAllByApiRepoVersionId(apiVersionInfo.getApiRepoVersionId())
						.stream()
						.map(apiComponent -> {
							Widget result = new Widget();
							result.setWidgetId(apiComponent.getId());
							result.setWidgetCode(apiComponent.getCode());
							result.setWidgetName(apiComponent.getName());
							
							result.setApiRepoId(apiVersionInfo.getApiRepoId());
							// 添加属性列表
							List<WidgetProperty> properties = apiComponentAttrDao.findAllByApiWidgetIdOrderByCode(apiComponent.getId()).stream().map(property -> {
								WidgetProperty each = new WidgetProperty();
								each.setCode(property.getCode());
								each.setName(property.getName());
								each.setValueType(property.getValueType());
								each.setDefaultValue(property.getDefaultValue());
								
								// 添加事件参数列表
								if(property.getValueType().equals(WidgetPropertyValueType.FUNCTION.getKey())) {
									List<EventArgument> eventArgs = apiComponentAttrFunArgDao.findAllByApiWidgetPropertyId(property.getId()).stream().map(eventArg -> {
										EventArgument ea = new EventArgument();
										ea.setCode(eventArg.getCode());
										ea.setName(eventArg.getName());
										ea.setValueType(eventArg.getValueType());
										ea.setDefaultValue(eventArg.getDefaultValue());
										return ea;
									}).collect(Collectors.toList());
									each.setArguments(eventArgs);
								}
								return each;
							}).collect(Collectors.toList());
							result.setProperties(properties);
							return result;
						}).collect(Collectors.toList());
				// 对部件进行分组
				// 当前都归到未分类下
				WidgetCategory category = new WidgetCategory();
				category.setName("_");
				category.setWidgets(widgets);
				
				RepoWidgetList widgetRepo = new RepoWidgetList();
				widgetRepo.setApiRepoId(apiVersionInfo.getApiRepoId());
				widgetRepo.setApiRepoName(apiVersionInfo.getApiRepoName());
				widgetRepo.setWidgetCategories(Collections.singletonList(category));
				return widgetRepo;
			}).collect(Collectors.toList());
	}

	@Override
	public List<ProjectDependence> findAllByProjectId(Integer repoId, RepositoryResource project) {
		// 之前一个仓库中只能创建一个项目，所以直接使用仓库标识获取
		// 现在一个仓库中能创建多个项目，所以需要联合使用仓库标识和项目标识获取
		List<ProjectDependence> result = projectDependenceDao.findAllByProjectId(repoId); // TODO: 改为根据 repoId 和 projectId 查询
		
		// 将系统使用的标准库依赖添加到最前面
		// 获取最新的依赖版本号
		
		String stdIdeRepoUrl = null;
		
		if(project.getAppType() == AppType.MINI_PROGRAM) {
			stdIdeRepoUrl = CmPropKey.STD_MINI_PROGRAM_COMPONENT_IDE_GIT_URL;
		} else if(project.getAppType() == AppType.WEB) {
			stdIdeRepoUrl = CmPropKey.STD_WIDGET_IDE_GIT_URL;
		}
		
		Integer createUserId = propertyService.findIntegerValue(CmPropKey.STD_REPO_REGISTER_USER_ID, 1);

		componentRepoDao.findByGitRepoUrlAndCreateUserId(stdIdeRepoUrl, createUserId).flatMap(componentRepo -> {
			return componentRepoVersionService.findLatestVersion(componentRepo.getId());
		}).ifPresent(componentRepoVersion -> {
			ProjectDependence stdIdeWidgetRepo = new ProjectDependence();
			stdIdeWidgetRepo.setComponentRepoVersionId(componentRepoVersion.getId());
			// TODO: 联合使用 repoId 和 projectId
			stdIdeWidgetRepo.setProjectId(repoId);
			result.add(0, stdIdeWidgetRepo);
		});
		
		return result;
	}
	
	/**
	 * 获取 build 版的标准库，当前实现只支持 dojo 版的标准库。
	 * 
	 * 1. 标准库
	 * 2. Default Profile
	 * 3. Build
	 * 
	 * @return
	 */
	private Optional<ProjectDependence> findProjectBuildDependenceByStandardAndDefaultProfile() {
		String stdBuildRepoUrl = propertyService.findStringValue(CmPropKey.STD_WIDGET_BUILD_DOJO_GIT_URL, "");
		Integer createUserId = propertyService.findIntegerValue(CmPropKey.STD_REPO_REGISTER_USER_ID, 1);
		// 事先要在组件仓库中注册 std-widget-web。
		// 标准库，永远只使用最新版。
		
		return componentRepoDao.findByGitRepoUrlAndCreateUserId(stdBuildRepoUrl, createUserId)
				.flatMap(componentRepo -> componentRepoVersionService.findLatestVersion(componentRepo.getId()))
				.map(componentRepoVersion -> {
			ProjectDependence stdBuildWidgetRepo = new ProjectDependence();
			stdBuildWidgetRepo.setComponentRepoVersionId(componentRepoVersion.getId());
			return stdBuildWidgetRepo;
		});
	}

}
