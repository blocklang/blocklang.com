package com.blocklang.develop.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.constant.Constant;
import com.blocklang.core.constant.GitFileStatus;
import com.blocklang.core.git.GitFileInfo;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.util.IdGenerator;
import com.blocklang.core.util.StreamUtil;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.dao.PageWidgetAttrValueDao;
import com.blocklang.develop.dao.PageWidgetDao;
import com.blocklang.develop.dao.PageWidgetJdbcDao;
import com.blocklang.develop.dao.ProjectCommitDao;
import com.blocklang.develop.dao.ProjectDependenceDao;
import com.blocklang.develop.dao.ProjectResourceDao;
import com.blocklang.develop.data.UncommittedFile;
import com.blocklang.develop.designer.data.ApiRepoVersionInfo;
import com.blocklang.develop.designer.data.AttachedWidget;
import com.blocklang.develop.designer.data.AttachedWidgetProperty;
import com.blocklang.develop.designer.data.PageModel;
import com.blocklang.develop.model.PageWidget;
import com.blocklang.develop.model.PageWidgetAttrValue;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectCommit;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiComponentAttrDao;
import com.blocklang.marketplace.dao.ApiComponentDao;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiComponent;
import com.blocklang.marketplace.model.ApiRepo;

@Service
public class ProjectResourceServiceImpl implements ProjectResourceService {

	private static final Logger logger = LoggerFactory.getLogger(ProjectResourceServiceImpl.class);
	
	@Autowired
	private ProjectResourceDao projectResourceDao;
	@Autowired
	private ProjectCommitDao projectCommitDao;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private PageWidgetDao pageWidgetDao;
	@Autowired
	private PageWidgetJdbcDao pageWidgetJdbcDao;
	@Autowired
	private PageWidgetAttrValueDao pageWidgetAttrValueDao;
	@Autowired
	private ProjectDependenceDao projectDependenceDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiComponentDao apiComponentDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiComponentAttrDao apiComponentAttrDao;
	
	//@Transactional
	@Override
	public ProjectResource insert(Project project, ProjectResource resource) {
		if(resource.getSeq() == null) {
			Integer nextSeq = projectResourceDao.findFirstByProjectIdAndParentIdOrderBySeqDesc(resource.getProjectId(), resource.getParentId()).map(item -> item.getSeq() + 1).orElse(1);
			resource.setSeq(nextSeq);
		}
		ProjectResource result = projectResourceDao.save(resource);
		
		// 在 git 仓库中添加文件
		Integer parentResourceId = resource.getParentId();
		String relativeDir = parentResourceId == Constant.TREE_ROOT_ID ? null: this.findParentPath(parentResourceId);
		
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).map(rootDir -> {
			return new ProjectContext(project.getCreateUserName(), project.getName(), rootDir).getGitRepositoryDirectory();
		}).ifPresent(rootPath -> {
			Path path = rootPath;
			if(StringUtils.isNotBlank(relativeDir)) {
				path = path.resolve(relativeDir);
			}
			if(resource.isGroup()) {
				path = path.resolve(resource.getKey());
				try {
					Files.createDirectory(path);
				} catch (IOException e) {
					logger.error("创建分组文件夹时出错！", e);
				}
			} else {
				path = path.resolve(result.getFileName());
				try {
					Files.writeString(path, "{}", StandardOpenOption.CREATE);
				} catch (IOException e) {
					logger.error("为页面生成 json文件时出错！", e);
				}
			}
			
		});
		
		return result;
	}

	@Override
	public List<ProjectResource> findChildren(Project project, Integer parentResourceId) {
		if(project == null) {
			return new ArrayList<ProjectResource>();
		}
		
		List<ProjectResource> result = projectResourceDao.findByProjectIdAndParentIdOrderByResourceTypeAscSeqAsc(project.getId(), parentResourceId);
		if(result.isEmpty()) {
			return new ArrayList<ProjectResource>();
		}
		
		String relativeDir = parentResourceId == Constant.TREE_ROOT_ID ? "": this.findParentPath(parentResourceId);
		
		Optional<ProjectContext> projectContext = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).map(rootDir -> {
			return new ProjectContext(project.getCreateUserName(), project.getName(), rootDir);
		});
				
		List<GitFileInfo> files = projectContext
				.map(context -> GitUtils.getFiles(context.getGitRepositoryDirectory(), relativeDir))
				.orElse(new ArrayList<GitFileInfo>());
		// 根据文件名关联
		// fileMap 的 key 不包含父目录
		Map<String, GitFileInfo> fileMap = files.stream().collect(Collectors.toMap(GitFileInfo::getName, Function.identity()));
		// fileStatusMap 的 key 中包含父目录，所有父目录
		Map<String, GitFileStatus> fileStatusMap = projectContext
				.map(context -> GitUtils.status(context.getGitRepositoryDirectory(), relativeDir))
				.orElse(Collections.emptyMap());
		
		result.forEach(resource -> {
			if(StringUtils.isBlank(resource.getName())) {
				resource.setName(resource.getKey());
			}
			GitFileInfo fileInfo = null;
			GitFileStatus status = null;
			if(resource.isGroup()) {
				// 当文件夹未跟踪时，则下面的子文件夹不会再在查询结果中，但也可以归为未跟踪。
				// 所以，如果找不到当前文件夹的状态，则继承父文件夹的状态
				String path = null;
				if(StringUtils.isBlank(relativeDir)) {
					path = resource.getKey();
				} else {
					path = relativeDir + "/" + resource.getKey();
				}
				fileInfo = fileMap.get(resource.getKey());
				status = fileStatusMap.get(path);
				
				// 查找子节点的状态
				// 如果目录中同时有新增和修改，则显示修改颜色
				// 如果目录中只有新增，则显示新增颜色
				if(status == null) {
					for(Map.Entry<String, GitFileStatus> entry : fileStatusMap.entrySet()) {
						if(!entry.getKey().equals(path) && entry.getKey().startsWith(path + "/")) {
							if(entry.getValue() == GitFileStatus.MODIFIED || entry.getValue() == GitFileStatus.CHANGED) {
								status = entry.getValue();
								break;
							} else if(entry.getValue() == GitFileStatus.UNTRACKED || entry.getValue() == GitFileStatus.ADDED) {
								status = entry.getValue();
							}
						}
					}
				}
				
				if(status == GitFileStatus.UNTRACKED) {
					// 如果文件夹下有未跟踪的内容，则显示为未跟踪，否则设置为 null
					boolean hasUntrackedFile = false;
					for(Map.Entry<String, GitFileStatus> entry : fileStatusMap.entrySet()) {
						if(!entry.getKey().equals(path) && entry.getKey().startsWith(path + "/")) {
							if(entry.getValue() == GitFileStatus.UNTRACKED) {
								hasUntrackedFile = true;
								break;
							}
						}
					}
					if(!hasUntrackedFile) {
						status = null;
					}
				}
				
				// 此时是多级目录都没有跟踪
				if(fileInfo == null && status == null) {
					// 目录没有被跟踪，则不显示状态信息
				}
				
			} else {
				String path = null;
				if(StringUtils.isBlank(relativeDir)) {
					path = resource.getFileName();
				} else {
					path = relativeDir + "/" + resource.getFileName();
				}
				fileInfo = fileMap.get(resource.getFileName());
				status = fileStatusMap.get(path);
			}
			
			if(fileInfo != null) {
				resource.setLatestCommitId(fileInfo.getCommitId());
				resource.setLatestCommitTime(fileInfo.getLatestCommitTime());
				resource.setLatestShortMessage(fileInfo.getLatestShortMessage());
				resource.setLatestFullMessage(fileInfo.getLatestFullMessage());
			}
			resource.setGitStatus(status);
		});
		
		return result;
	}

	@Override
	public String findParentPath(Integer resourceId) {
		List<String> pathes = new ArrayList<String>();
		
		while(resourceId != Constant.TREE_ROOT_ID) {
			resourceId = projectResourceDao.findById(resourceId).map(resource -> {
				if(resource.isDependence()) {
					pathes.add(0, resource.getName());
				} else {
					pathes.add(0, resource.getKey());
				}
				
				return resource.getParentId();
			}).orElse(Constant.TREE_ROOT_ID);
		}
		
		return String.join("/", pathes);
	}

	@Override
	public Optional<ProjectResource> findByKey(
			Integer projectId, 
			Integer parentId, 
			ProjectResourceType resourceType,
			AppType appType,
			String key) {
		return projectResourceDao.findByProjectIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
				projectId, 
				parentId, 
				resourceType,
				appType,
				key);
	}
	
	@Override
	public Optional<ProjectResource> findByName(
			Integer projectId, 
			Integer parentId, 
			ProjectResourceType resourceType,
			AppType appType, 
			String name) {
		return projectResourceDao.findByProjectIdAndParentIdAndResourceTypeAndAppTypeAndNameIgnoreCase(
				projectId, 
				parentId, 
				resourceType,
				appType,
				name);
	}

	@Override
	public Optional<ProjectResource> findById(Integer resourceId) {
		return projectResourceDao.findById(resourceId);
	}

	@Override
	public List<ProjectResource> findParentGroupsByParentPath(Integer projectId, String parentPath) {
		List<ProjectResource> result = new ArrayList<ProjectResource>();
		
		if(projectId == null) {
			return result;
		}
		if(StringUtils.isBlank(parentPath)) {
			return result;
		}
		
		String[] keys = parentPath.trim().split("/");
		Integer parentId = Constant.TREE_ROOT_ID;
		boolean allMatched = true;
		for(String key : keys) {
			Optional<ProjectResource> resourceOption = findByKey(projectId, parentId, ProjectResourceType.GROUP, AppType.UNKNOWN, key);
			if(resourceOption.isEmpty()) {
				allMatched = false;
				break;
			}
			result.add(resourceOption.get());
			parentId = resourceOption.get().getId();
		}
		
		if(!allMatched) {
			return new ArrayList<ProjectResource>();
		}
		
		return result;
	}
	
	private List<ProjectResource> findParentGroupsByParentPath(List<ProjectResource> projectResources, String parentPath) {
		if(projectResources.isEmpty()) {
			return Collections.emptyList();
		}

		List<ProjectResource> result = new ArrayList<ProjectResource>();
		
		String[] keys = parentPath.trim().split("/");
		Integer parentId = Constant.TREE_ROOT_ID;
		boolean allMatched = true;
		for(String key : keys) {
			Integer finalParentId = parentId;
			Optional<ProjectResource> resourceOption = projectResources.stream()
					.filter(item -> item.getParentId().equals(finalParentId)
							&& item.getResourceType().equals(ProjectResourceType.GROUP)
							&& item.getAppType().equals(AppType.UNKNOWN) 
							&& item.getKey().equalsIgnoreCase(key))
					.findFirst();

			if(resourceOption.isEmpty()) {
				allMatched = false;
				break;
			}
			result.add(resourceOption.get());
			parentId = resourceOption.get().getId();
		}
		
		if(!allMatched) {
			return Collections.emptyList();
		}
		
		return result;
	}

	@Override
	public List<UncommittedFile> findChanges(Project project) {
		Map<String, GitFileStatus> fileStatusMap = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)
				.map(rootDir -> new ProjectContext(project.getCreateUserName(), project.getName(), rootDir))
				.map(context -> GitUtils.status(context.getGitRepositoryDirectory(), ""))
				.orElse(Collections.emptyMap());
		
		final List<ProjectResource> allResources = fileStatusMap.isEmpty() ? Collections.emptyList() : projectResourceDao.findAllByProjectId(project.getId());
		
		return fileStatusMap.entrySet().stream().filter(item -> {
			String filePath = item.getKey();
			// 排除分组
			if(filePath.endsWith(".page.web.json")) {
				return true;
			}
			if(filePath.equalsIgnoreCase(ProjectResource.README_NAME)) {
				return true;
			}
			if(filePath.equalsIgnoreCase(ProjectResource.DEPENDENCE_NAME)) {
				return true;
			}
			
			// 到此处，都当成是分组
			return false;
		}).map(item -> {
			// 根据文件名反推资源信息
			String filePath = item.getKey();
			
			ProjectResourceType resourceType = null;
			AppType appType = null;
			String resourceKey = null;
			String parentPath = null;
			if(filePath.endsWith(".page.web.json")) {
				resourceType = ProjectResourceType.PAGE;
				appType = AppType.WEB;
				String stripedFilenameExtension = filePath.substring(0, filePath.length() - ".page.web.json".length());
				int lastIndex = stripedFilenameExtension.lastIndexOf("/");
				if(lastIndex == -1) {
					resourceKey = stripedFilenameExtension;
					parentPath = "";
				} else {
					resourceKey = stripedFilenameExtension.substring(lastIndex + 1/*去掉/*/);
					parentPath = stripedFilenameExtension.substring(0, lastIndex);
				}
			} else if(filePath.equalsIgnoreCase(ProjectResource.README_NAME)){
				// README.md 文件只存在于根目录
				resourceType = ProjectResourceType.FILE;
				appType = AppType.UNKNOWN;
				resourceKey = ProjectResource.README_KEY;
				parentPath = "";
			} else if(filePath.equalsIgnoreCase(ProjectResource.DEPENDENCE_NAME)) {
				// DEPENDENCE.json 文件只存在于根目录
				resourceType = ProjectResourceType.DEPENDENCE;
				appType = AppType.UNKNOWN;
				resourceKey = ProjectResource.DEPENDENCE_KEY;
				parentPath = "";
			}
			
			Integer parentId = Constant.TREE_ROOT_ID;
			String parentNamePath = "";
			if(StringUtils.isNotBlank(parentPath)) {
				List<ProjectResource> parentGroups = findParentGroupsByParentPath(allResources, parentPath);
				if(parentGroups.isEmpty()) {
					logger.error("在 git 仓库中存在 " + parentPath + "，但是在资源表中没有找到");
					return null;
				}
				parentId = parentGroups.get(parentGroups.size() - 1).getId();
				for(ProjectResource each : parentGroups) {
					parentNamePath += (StringUtils.isBlank(each.getName()) ? each.getKey() : each.getName()) + "/";
				}
			}

			Integer finalParentId = parentId;
			String finalResourceKey = resourceKey;
			ProjectResourceType finalResourceType = resourceType;
			AppType finalAppType = appType;
			
			// 找到资源文件
			Optional<ProjectResource> resourceOption = allResources
					.stream()
					.filter(eachResource -> eachResource.getParentId().equals(finalParentId) && 
							eachResource.getKey().equalsIgnoreCase(finalResourceKey) && 
							eachResource.getResourceType().equals(finalResourceType) && 
							eachResource.getAppType().equals(finalAppType))
					.findFirst();
			if(resourceOption.isEmpty()) {
				logger.error("在 git 仓库中存在 " + parentPath + "/" + resourceKey + "，但是在资源表中没有找到");
				return null;
			}
			
			UncommittedFile file = new UncommittedFile();
			ProjectResource resource = resourceOption.get();
			file.setIcon(resource.getIcon());
			file.setFullKeyPath(filePath);
			file.setGitStatus(item.getValue());
			file.setResourceName(StringUtils.isBlank(resource.getName()) ? resource.getKey() : resource.getName());
			file.setParentNamePath(parentNamePath);

			return file;
		}).collect(Collectors.toList());
	}

	@Override
	public void stageChanges(Project project, String[] filePathes) {
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)
		.ifPresent(rootDir -> {
			ProjectContext context = new ProjectContext(project.getCreateUserName(), project.getName(), rootDir);
			GitUtils.add(context.getGitRepositoryDirectory(), filePathes);
		});
	}

	@Override
	public void unstageChanges(Project project, String[] filePathes) {
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)
		.ifPresent(rootDir -> {
			ProjectContext context = new ProjectContext(project.getCreateUserName(), project.getName(), rootDir);
			GitUtils.reset(context.getGitRepositoryDirectory(), filePathes);
		});
	}

	@Override
	public String commit(UserInfo user, Project project, String commitMessage) {
		String commitId = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)
			.map(rootDir -> {
				ProjectContext context = new ProjectContext(project.getCreateUserName(), project.getName(), rootDir);
				return GitUtils.commit(context.getGitRepositoryDirectory(), user.getLoginName(), user.getEmail(), commitMessage);
			}).orElse(null);
		
		if(commitId != null) {
			ProjectCommit commit = new ProjectCommit();
			commit.setCommitId(commitId);
			commit.setCommitUserId(user.getId());
			commit.setCommitTime(LocalDateTime.now());
			commit.setProjectId(project.getId());
			commit.setBranch(Constants.MASTER);
			commit.setShortMessage(commitMessage);
			commit.setCreateUserId(user.getId());
			commit.setCreateTime(LocalDateTime.now());
			
			projectCommitDao.save(commit);
		}
		
		return commitId;
	}

	@Transactional
	@Override
	public void updatePageModel(PageModel pageModel) {
		Integer pageId = pageModel.getPageId();
		
		List<AttachedWidget> widgets = pageModel.getWidgets();
		// 1. 先全部删除
		// 注意：删除代码不要放在 !widgets.isEmpty 判断内
		pageWidgetJdbcDao.deleteWidgetProperties(pageId);
		pageWidgetJdbcDao.deleteWidgets(pageId);
		
		if(!widgets.isEmpty()) {
			List<PageWidgetAttrValue> properties = new ArrayList<>();
			widgets.forEach(widget -> {
				widget.getProperties().forEach(prop -> {
					PageWidgetAttrValue p = new PageWidgetAttrValue();
					p.setPageWidgetId(widget.getId());
					p.setId(prop.getId());
					p.setWidgetAttrCode(prop.getCode());
					p.setAttrValue(prop.getValue());
					p.setExpr(prop.isExpr());
					properties.add(p);
				});
			});
			// 2. 然后再新增
			pageWidgetJdbcDao.batchSaveWidgets(pageId, widgets);
			pageWidgetJdbcDao.batchSaveWidgetProperties(properties);
		}
		
	}

	// TODO: 此处需要性能优化
	@Override
	public PageModel getPageModel(Integer projectId, Integer pageId) {
		PageModel model = new PageModel();
		
		model.setPageId(pageId);
		
		List<PageWidget> pageWidgets = pageWidgetDao.findAllByPageIdOrderBySeq(pageId);
		
		if(pageWidgets.isEmpty()) {
			return model;
		}
		
		Map<Integer, List<ApiComponent>> cachedAndGroupedWidgets = new HashMap<>();
		// 如果页面模型中存在部件，则获取项目依赖的所有部件列表
		// 然后根据这个列表来匹配
		projectDependenceDao
			// 1. 获取项目的所有依赖
			.findAllByProjectId(projectId)
			.stream()
			// 2. 找出对应的组件仓库的版本信息，就可获取到 API 仓库的版本信息
			.flatMap(item -> {
				return componentRepoVersionDao.findById(item.getComponentRepoVersionId()).stream();
			})
			// 3. 针对 api repo version 去重，而不是针对 api repo 去重
			.filter(StreamUtil.distinctByKey(item -> item.getApiRepoVersionId()))
			// 4. 找到对应 api repo，然后过滤出其中的 widget 仓库
			.map(item -> {
				Optional<ApiRepo> apiRepoOption = apiRepoVersionDao.findById(item.getApiRepoVersionId())
						.flatMap(apiRepoVersion -> apiRepoDao.findById(apiRepoVersion.getApiRepoId()));
				ApiRepoVersionInfo result = new ApiRepoVersionInfo();
				result.setApiRepoVersionId(item.getApiRepoVersionId());
				apiRepoOption.ifPresent(apiRepo -> {
					result.setApiRepoName(apiRepo.getName());
					result.setApiRepoId(apiRepo.getId());
					result.setCategory(apiRepo.getCategory());
				});
				return result;
			})
			.filter(apiVersionInfo -> apiVersionInfo.getCategory() == RepoCategory.WIDGET)
			.forEach(apiVersionInfo -> {
				// 4. 获取到该版本下的所有部件
				List<ApiComponent> widgets = apiComponentDao.findAllByApiRepoVersionId(apiVersionInfo.getApiRepoVersionId());
				cachedAndGroupedWidgets.put(apiVersionInfo.getApiRepoId(), widgets);
			});
		
		List<AttachedWidget> convertedWidgets = pageWidgets.stream().map(item -> {
			AttachedWidget result = new AttachedWidget();
			result.setId(item.getId());
			result.setParentId(item.getParentId());
			result.setWidgetCode(item.getWidgetCode());
			result.setApiRepoId(item.getApiRepoId());
			
			// 5. 部件的属性，延迟加载，只有页面中使用到了部件，才加载其属性
			// 注意，如果一个部件在页面中使用了多次，最好只加载一次属性，可使用缓存优化
			cachedAndGroupedWidgets.get(item.getApiRepoId())
				.stream()
				.filter(component -> component.getCode().equals(item.getWidgetCode()))
				.findFirst()
				.ifPresent(component -> {
					String widgetName = component.getLabel();
					if(StringUtils.isBlank(widgetName)) {
						widgetName = component.getName();
					}
					result.setWidgetName(widgetName);
					result.setWidgetId(component.getId());
					result.setCanHasChildren(component.getCanHasChildren());

					List<PageWidgetAttrValue> attachedProperties = pageWidgetAttrValueDao.findAllByPageWidgetId(item.getId());
					
					List<AttachedWidgetProperty> properties = apiComponentAttrDao
							.findAllByApiComponentIdOrderByCode(component.getId())
							.stream()
							.map(componentAttr -> {
								// 注意，属性列表要先获取部件的属性列表，然后再赋值，确保新增的属性（页面模型中未添加）也能包括进来
								// 部件属性基本信息
								AttachedWidgetProperty property = new AttachedWidgetProperty();
								property.setCode(componentAttr.getCode());
								
								// 如果 label 有值，则优先取 label 值，否则取 name 的值
								String attrName = componentAttr.getLabel();
								if(StringUtils.isBlank(attrName)) {
									attrName = componentAttr.getName();
								}
								property.setName(attrName);
								property.setValueType(componentAttr.getValueType().getKey());
								// 部件属性的实例信息
								attachedProperties
									.stream()
									.filter(pageWidgetAttrValue -> pageWidgetAttrValue.getWidgetAttrCode().equals(componentAttr.getCode()))
									.findFirst().ifPresentOrElse(matchedAttr -> {
										property.setId(matchedAttr.getId());
										// 如果实例中没有设置值，则取默认值，如果没有默认值，则保持为 null
										String value = matchedAttr.getAttrValue();
										if(StringUtils.isBlank(value)) {
											value = componentAttr.getDefaultValue();
										}
										property.setValue(value);
									}, () -> {
										// id 的值，如果是新增属性，则在此处自动生成一个 id
										property.setId(IdGenerator.uuid());
										// 如果实例中没有设置值，则取默认值，否则保持为 null
										property.setValue(componentAttr.getDefaultValue());
									});
								return property;
							})
							.collect(Collectors.toList());
					result.setProperties(properties); 
				});
			
			return result;
		}).collect(Collectors.toList());
		
		model.setPageId(pageId);
		model.setWidgets(convertedWidgets);
		return model;
	}

}
