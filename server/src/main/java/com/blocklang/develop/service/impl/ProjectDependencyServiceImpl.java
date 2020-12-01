package com.blocklang.develop.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.service.PropertyService;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.dao.ProjectBuildProfileDao;
import com.blocklang.develop.dao.ProjectDependencyDao;
import com.blocklang.develop.data.ProjectDependencyData;
import com.blocklang.develop.designer.data.ApiRepoVersionInfo;
import com.blocklang.develop.designer.data.EventArgument;
import com.blocklang.develop.designer.data.RepoWidgetList;
import com.blocklang.develop.designer.data.Widget;
import com.blocklang.develop.designer.data.WidgetCategory;
import com.blocklang.develop.designer.data.WidgetProperty;
import com.blocklang.develop.model.ProjectDependency;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryContext;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.ProjectDependencyService;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.RepoType;
import com.blocklang.marketplace.constant.WidgetPropertyValueType;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ApiWidgetDao;
import com.blocklang.marketplace.dao.ApiWidgetEventArgDao;
import com.blocklang.marketplace.dao.ApiWidgetPropertyDao;
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
public class ProjectDependencyServiceImpl implements ProjectDependencyService{
	
	private static final Logger logger = LoggerFactory.getLogger(ProjectDependencyServiceImpl.class);

	@Autowired
	private ProjectDependencyDao projectDependencyDao;
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
	public Boolean devDependencyExists(Integer projectId, Integer componentRepoId) {
		List<ComponentRepoVersion> componentRepoVersions = componentRepoVersionDao.findAllByComponentRepoId(componentRepoId);
		if(componentRepoVersions.isEmpty()) {
			return false;
		}
		List<ProjectDependency> devDependencies = projectDependencyDao.findAllByProjectIdAndProfileId(projectId, null);
		if(devDependencies.isEmpty()) {
			return false;
		}
		
		return componentRepoVersions.stream()
				.anyMatch(version -> devDependencies.stream()
						.anyMatch(dependency -> version.getId().equals(dependency.getComponentRepoVersionId())));
	}
	
	@Override
	public Boolean buildDependencyExists(Integer projectId, Integer buildProfileId, Integer componentRepoId) {
		List<ComponentRepoVersion> componentRepoVersions = componentRepoVersionDao.findAllByComponentRepoId(componentRepoId);
		if(componentRepoVersions.isEmpty()) {
			return false;
		}
		List<ProjectDependency> devDependencies = projectDependencyDao.findAllByProjectIdAndProfileId(projectId, buildProfileId);
		if(devDependencies.isEmpty()) {
			return false;
		}
		
		return componentRepoVersions.stream()
				.anyMatch(version -> devDependencies.stream()
						.anyMatch(dependency -> version.getId().equals(dependency.getComponentRepoVersionId())));
	}
	
	@Override
	public ProjectDependency save(Repository repository, RepositoryResource project, ProjectDependency dependency) {
		ProjectDependency savedDependency = projectDependencyDao.save(dependency);
		updateProjectDependencyFile(repository, project);
		return savedDependency;
	}
	
	//```json
	//{
	//    "dev": {
	//        "github/@publisher1/repoName1": {"git": "", "tag": "v0.1.0"},
	//        "gitee/@publisher2/repoName2": {"git": "", "tag": "v0.1.0"}
	//    },
	//    "build": {
	//        "wechatMiniApp":{
	//            "default": {
	//                "github/@publisher1/repoName1": {"git": "", "tag": "v0.1.0"},
	//                "github/@publisher2/repoName2": {"git": "", "tag": "v0.1.0"}
	//            },
	//            "profile2": {
	//                "github/@publisher11/repoName11": {"gait": "", "tag": "v0.1.0"},
	//                "github/@publisher22/repoName22": {"git": "", "tag": "v0.1.0"}
	//            }
	//        }
	//    }
	//}
	//```
	/**
	 * DEPENDENCY.json 文件中不存储标准库
	 * 
	 * @param repository
	 * @param project
	 * @param dependency
	 */
	private void updateProjectDependencyFile(Repository repository, RepositoryResource project) {
		// 获取项目的所有依赖
		List<ProjectDependencyData> dependencies = findAllConfigDependencies(project.getId());
		appendBuildProfile(dependencies);
		Map<String, Object> dependencyJsonObject = convertToDependencyJsonFile(dependencies);
		
		String fileName = RepositoryResource.DEPENDENCY_NAME;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dependencyJsonObject);
			propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).map(rootDir -> {
				return new RepositoryContext(repository.getCreateUserName(), repository.getName(), rootDir).getGitRepositoryDirectory();
			}).ifPresent(rootPath -> {
				Path path = rootPath.resolve(project.getName()).resolve(fileName);
				try {
					Files.writeString(path, jsonContent);
				} catch (IOException e) {
					logger.error("往 " + fileName + " 文件写入内容时出错", e);
				}
			});
		} catch (JsonProcessingException e) {
			logger.error("转换为 json 字符串时出错", e);
		}
	}

	private Map<String, Object> convertToDependencyJsonFile(List<ProjectDependencyData> dependencies) {
		if(dependencies.isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		List<ProjectDependencyData> devDependencies = dependencies
				.stream()
				.filter(dependency -> dependency.getComponentRepo().getRepoType().equals(RepoType.IDE))
				.collect(Collectors.toList());
		
		Map<String, Object> devMap = new HashMap<String, Object>();
		for(ProjectDependencyData data : devDependencies) {
			ComponentRepo componentRepo = data.getComponentRepo();
			String dependencyKey = componentRepo.getGitRepoWebsite() 
					+ "/" 
					+ componentRepo.getGitRepoOwner() 
					+ "/"
					+ componentRepo.getGitRepoName();

			// 当前仅支持 git
			Map<String, String> git = new HashMap<String, String>();
			git.put("git", data.getComponentRepo().getGitRepoUrl());
			git.put("tag", data.getComponentRepoVersion().getGitTagName());
			devMap.put(dependencyKey, git);
		}

		List<ProjectDependencyData> buildDependencies = dependencies
				.stream()
				.filter(dependency -> !dependency.getComponentRepo().getRepoType().equals(RepoType.IDE))
				.collect(Collectors.toList());
		
		Map<String, Map<String, List<ProjectDependencyData>>> groupedBuildDependencies = buildDependencies
				.stream()
				.collect(Collectors.groupingBy(dependencyData -> dependencyData.getProfile().getBuildTarget().getKey(), 
								Collectors.groupingBy(dependencyData -> dependencyData.getProfile().getName())));

		Map<String, Object> buildMap = new HashMap<String, Object>();
		for(Map.Entry<String, Map<String, List<ProjectDependencyData>>> groupedByBuildTarget : groupedBuildDependencies.entrySet()) {
			String buildTarget = groupedByBuildTarget.getKey();
			Map<String, Object> profileNameMap = new HashMap<String, Object>();
			for(Map.Entry<String, List<ProjectDependencyData>> groupedByProfile : groupedByBuildTarget.getValue().entrySet()) {
				String profileValue = groupedByProfile.getKey();
				Map<String, Object> profileMap = new HashMap<String, Object>();
				for(ProjectDependencyData data : groupedByProfile.getValue()) {
					ComponentRepo componentRepo = data.getComponentRepo();
					String dependencyKey = componentRepo.getGitRepoWebsite() 
							+ "/" 
							+ componentRepo.getGitRepoOwner()
							+ "/" 
							+ componentRepo.getGitRepoName();
					
					Map<String, Object> dependencyMap = new HashMap<String, Object>();
					dependencyMap.put("git", componentRepo.getGitRepoUrl());
					dependencyMap.put("tag", data.getComponentRepoVersion().getGitTagName());
					
					profileMap.put(dependencyKey, dependencyMap);
				}
				profileNameMap.put(profileValue, profileMap);
			}
			buildMap.put(buildTarget, profileNameMap);
		}
		
		if(!devMap.isEmpty()) {
			result.put("dev", devMap);
		}
		if(!buildMap.isEmpty()) {
			result.put("build", buildMap);
		}
		return result;
	}
	
	// 补充 profile 信息
	private void appendBuildProfile(List<ProjectDependencyData> dependencies) {
		for(ProjectDependencyData data : dependencies) {
			// 先获取依赖信息
			ProjectDependency dependency = data.getDependency();
			// 然后根据依赖信息获取 profile 信息
			if(dependency.getProfileId() != null) {
				projectBuildProfileDao.findById(dependency.getProfileId()).ifPresent(profile -> data.setProfile(profile));
			}
		}
	}

	private List<ProjectDependencyData> convert(List<ProjectDependency> dependencies) {
		return dependencies.stream().map(dependency -> {
			ComponentRepoVersion componentRepoVersion = null;
			ComponentRepo componentRepo = null;
			ApiRepo apiRepo = null;
			ApiRepoVersion apiRepoVersion = null;
			
			componentRepoVersion = componentRepoVersionDao.findById(dependency.getComponentRepoVersionId()).orElse(null);
			if(componentRepoVersion != null) {
				componentRepo = componentRepoDao.findById(componentRepoVersion.getComponentRepoId()).orElse(null);
				apiRepoVersion = apiRepoVersionDao.findById(componentRepoVersion.getApiRepoVersionId()).orElse(null);
			}
			if(apiRepoVersion != null) {
				apiRepo = apiRepoDao.findById(apiRepoVersion.getApiRepoId()).orElse(null);
			}
			
			return new ProjectDependencyData(dependency, componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
		}).collect(Collectors.toList());
	}

	@Override
	public void delete(Repository repository, RepositoryResource project, Integer dependencyId) {
		projectDependencyDao.findById(dependencyId).ifPresent(dependency -> {
			projectDependencyDao.delete(dependency);
			updateProjectDependencyFile(repository, project);
		});
	}

	@Override
	public ProjectDependency update(Repository repository, RepositoryResource project, ProjectDependency dependency) {
		ProjectDependency result = projectDependencyDao.save(dependency);
		updateProjectDependencyFile(repository, project);
		return result;
	}

	@Override
	public Optional<ProjectDependency> findById(Integer dependencyId) {
		return projectDependencyDao.findById(dependencyId);
	}
	
	@Override
	public List<RepoWidgetList> findAllWidgets(Integer projectId) {
		// 获取项目的所有依赖，包含组件仓库的版本信息
		List<ProjectDependency> allDependencies = projectDependencyDao.findAllByProjectId(projectId);
		
		// 转换为对应的 API 仓库的版本信息
		return allDependencies
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
	public List<ProjectDependencyData> findAllConfigDependencies(Integer projectId) {
		List<ProjectDependency> dependencies = projectDependencyDao.findAllByProjectId(projectId);
		return convert(dependencies);
	}

	@Override
	public List<ProjectDependencyData> findDevDependencies(Integer projectId) {
		List<ProjectDependency> dependencies = projectDependencyDao.findAllByProjectIdAndProfileId(projectId, null);
		return convert(dependencies);
	}

	// 事先要在组件仓库中注册 std-widget-web。
	// 标准库，永远只使用最新版。
	@Override
	public List<ProjectDependencyData> findStdDevDependencies(AppType appType) {
		return convert(getStdDevDependencies(appType));
	}

	private List<ProjectDependency> getStdDevDependencies(AppType appType) {
		Integer createUserId = propertyService.findIntegerValue(CmPropKey.STD_REPO_REGISTER_USER_ID, 1);
		String stdDevRepoUrlKey = null;
		if(AppType.WEB.equals(appType)) {
			stdDevRepoUrlKey = CmPropKey.STD_WIDGET_IDE_GIT_URL;
		} else if(AppType.MINI_PROGRAM.equals(appType)) {
			stdDevRepoUrlKey = CmPropKey.STD_MINI_PROGRAM_COMPONENT_IDE_GIT_URL;
		}
		String stdDevRepoUrl = propertyService.findStringValue(stdDevRepoUrlKey, "");
		Optional<ProjectDependency> dependencyOption = componentRepoDao.findByGitRepoUrlAndCreateUserId(stdDevRepoUrl, createUserId)
			.flatMap(componentRepo -> componentRepoVersionService.findByComponentIdAndVersion(componentRepo.getId(), Constants.MASTER)) // 注意，默认只依赖 master 分支
			.map(componentRepoVersion -> {
				ProjectDependency dependency = new ProjectDependency();
				dependency.setComponentRepoVersionId(componentRepoVersion.getId());
				return dependency;
			});
		if(dependencyOption.isEmpty()) {
			return Collections.emptyList();
		}
		return Collections.singletonList(dependencyOption.get());
	}

	@Override
	public List<ProjectDependencyData> findStdBuildDependencies(AppType appType) {
		Integer createUserId = propertyService.findIntegerValue(CmPropKey.STD_REPO_REGISTER_USER_ID, 1);
		String stdDevRepoUrlKey = null;
		if(AppType.WEB.equals(appType)) {
			stdDevRepoUrlKey = CmPropKey.STD_WIDGET_BUILD_DOJO_GIT_URL;
		} else if(AppType.MINI_PROGRAM.equals(appType)) {
			stdDevRepoUrlKey = CmPropKey.STD_MINI_PROGRAM_COMPONENT_PROD_GIT_URL;
		}
		String stdDevRepoUrl = propertyService.findStringValue(stdDevRepoUrlKey, "");
		Optional<ProjectDependency> dependencyOption = componentRepoDao.findByGitRepoUrlAndCreateUserId(stdDevRepoUrl, createUserId)
			.flatMap(componentRepo -> componentRepoVersionService.findByComponentIdAndVersion(componentRepo.getId(), "master")) // 注意，默认只依赖 master 分支
			.map(componentRepoVersion -> {
				ProjectDependency dependency = new ProjectDependency();
				dependency.setComponentRepoVersionId(componentRepoVersion.getId());
				return dependency;
			});
		if(dependencyOption.isEmpty()) {
			return Collections.emptyList();
		}
		
		return convert(Collections.singletonList(dependencyOption.get()));
	}

	@Override
	public List<ProjectDependency> findAllDevDependencies(Integer projectId, AppType appType) {
		List<ProjectDependency> dependencies = projectDependencyDao.findAllByProjectIdAndProfileId(projectId, null);
		// 获取标准库
		dependencies.addAll(getStdDevDependencies(appType));
		return dependencies;
	}

}
