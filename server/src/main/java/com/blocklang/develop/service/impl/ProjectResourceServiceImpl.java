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
import com.blocklang.core.service.UserService;
import com.blocklang.core.util.IdGenerator;
import com.blocklang.core.util.StreamUtil;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.FlowType;
import com.blocklang.develop.constant.NodeCategory;
import com.blocklang.develop.constant.NodeLayout;
import com.blocklang.develop.constant.PortType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.dao.PageDataDao;
import com.blocklang.develop.dao.PageDataJdbcDao;
import com.blocklang.develop.dao.PageFunctionConnectionDao;
import com.blocklang.develop.dao.PageFunctionConnectionJdbcDao;
import com.blocklang.develop.dao.PageFunctionDao;
import com.blocklang.develop.dao.PageFunctionJdbcDao;
import com.blocklang.develop.dao.PageFunctionNodeDao;
import com.blocklang.develop.dao.PageFunctionNodeJdbcDao;
import com.blocklang.develop.dao.PageFunctionNodePortDao;
import com.blocklang.develop.dao.PageFunctionNodePortJdbcDao;
import com.blocklang.develop.dao.PageWidgetAttrValueDao;
import com.blocklang.develop.dao.PageWidgetDao;
import com.blocklang.develop.dao.PageWidgetJdbcDao;
import com.blocklang.develop.dao.ProjectCommitDao;
import com.blocklang.develop.dao.ProjectResourceDao;
import com.blocklang.develop.data.UncommittedFile;
import com.blocklang.develop.designer.data.ApiRepoVersionInfo;
import com.blocklang.develop.designer.data.AttachedWidget;
import com.blocklang.develop.designer.data.AttachedWidgetProperty;
import com.blocklang.develop.designer.data.DataPort;
import com.blocklang.develop.designer.data.EventArgument;
import com.blocklang.develop.designer.data.InputDataPort;
import com.blocklang.develop.designer.data.InputSequencePort;
import com.blocklang.develop.designer.data.NodeConnection;
import com.blocklang.develop.designer.data.OutputSequencePort;
import com.blocklang.develop.designer.data.PageEventHandler;
import com.blocklang.develop.designer.data.PageModel;
import com.blocklang.develop.designer.data.VisualNode;
import com.blocklang.develop.model.PageDataItem;
import com.blocklang.develop.model.PageFunction;
import com.blocklang.develop.model.PageFunctionConnection;
import com.blocklang.develop.model.PageFunctionNode;
import com.blocklang.develop.model.PageFunctionNodePort;
import com.blocklang.develop.model.PageWidget;
import com.blocklang.develop.model.PageWidgetAttrValue;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectCommit;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.marketplace.constant.WidgetPropertyValueType;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiWidgetPropertyDao;
import com.blocklang.marketplace.dao.ApiWidgetEventArgDao;
import com.blocklang.marketplace.dao.ApiWidgetDao;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiWidget;
import com.blocklang.marketplace.model.ApiWidgetEventArg;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.service.ApiRepoVersionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProjectResourceServiceImpl implements ProjectResourceService {

	private static final Logger logger = LoggerFactory.getLogger(ProjectResourceServiceImpl.class);
	
	@Autowired
	private ProjectResourceDao projectResourceDao;
	@Autowired
	private ProjectCommitDao projectCommitDao;
	@Autowired
	private UserService userService;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private PageWidgetDao pageWidgetDao;
	@Autowired
	private PageWidgetJdbcDao pageWidgetJdbcDao;
	@Autowired
	private PageWidgetAttrValueDao pageWidgetAttrValueDao;
	@Autowired
	private PageDataJdbcDao pageDataJdbcDao;
	@Autowired
	private PageDataDao pageDataDao;
	@Autowired
	private PageFunctionJdbcDao pageFunctionJdbcDao;
	@Autowired
	private PageFunctionDao pageFunctionDao;
	@Autowired
	private PageFunctionNodeJdbcDao pageFunctionNodeJdbcDao;
	@Autowired
	private PageFunctionNodeDao pageFunctionNodeDao;
	@Autowired
	private PageFunctionNodePortJdbcDao pageFunctionNodePortJdbcDao;
	@Autowired
	private PageFunctionNodePortDao pageFunctionNodePortDao;
	@Autowired
	private PageFunctionConnectionJdbcDao pageFunctionConnectionJdbcDao;
	@Autowired
	private PageFunctionConnectionDao pageFunctionConnectionDao;
	@Autowired
	private ProjectDependenceService projectDependenceService;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiWidgetDao apiComponentDao;
	@Autowired
	private ApiRepoVersionService apiRepoVersionService;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiWidgetPropertyDao apiComponentAttrDao;
	@Autowired
	private ApiWidgetEventArgDao apiComponentAttrFunArgDao;
	
	//@Transactional
	@Override
	public ProjectResource insert(Project project, ProjectResource resource) {
		if(resource.getSeq() == null) {
			Integer nextSeq = projectResourceDao.findFirstByProjectIdAndParentIdOrderBySeqDesc(resource.getProjectId(), resource.getParentId()).map(item -> item.getSeq() + 1).orElse(1);
			resource.setSeq(nextSeq);
		}
		ProjectResource result = projectResourceDao.save(resource);
		PageModel pageModel = this.createPageModelWithStdPage(result.getId());
		
		// 在 git 仓库中添加文件
		Integer parentResourceId = resource.getParentId();
		String relativeDir = parentResourceId == Constant.TREE_ROOT_ID ? null: String.join("/", this.findParentPathes(parentResourceId));
		
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
					ObjectMapper mapper = new ObjectMapper();
					String json = mapper.writeValueAsString(pageModel);
					Files.writeString(path, json, StandardOpenOption.CREATE);
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
		
		String relativeDir = parentResourceId == Constant.TREE_ROOT_ID ? "": String.join("/", this.findParentPathes(parentResourceId));
		
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
	public List<String> findParentPathes(Integer resourceId) {
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
		
		return pathes;
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
	public List<ProjectResource> findAllPages(Integer projectId, AppType appType) {
		return projectResourceDao.findAllByProjectIdAndAppTypeAndResourceType(projectId, appType, ProjectResourceType.PAGE);
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

	@Override
	public void updatePageModel(Project project, ProjectResource projectResource, PageModel pageModel) {
		this.updatePageModel(pageModel);
		if(project != null && projectResource != null) {
			this.updatePageFileInGit(project, projectResource, pageModel);
		}
	}
	
	// FIXME: @Transactional 加在此处不会生效
	// TODO: 将一些公共方法单独存在一个 service 类中
	@Transactional
	private void updatePageModel(PageModel pageModel) {
		Integer pageId = pageModel.getPageId();
		
		// 一. 先全部删除
		// 注意：删除代码不要放在 !widgets.isEmpty 判断内
		// 删除部件
		pageWidgetJdbcDao.deleteWidgetProperties(pageId);
		pageWidgetJdbcDao.deleteWidgets(pageId);
		// 删除数据
		pageDataJdbcDao.delete(pageId);
		// 删除函数
		// 1. 删除连接
		pageFunctionConnectionJdbcDao.deleteByPageId(pageId);
		// 2. 删除 port
		pageFunctionNodePortJdbcDao.deleteByPageId(pageId);
		// 3. 删除 node
		pageFunctionNodeJdbcDao.deleteByPageId(pageId);
		// 4. 删除函数
		pageFunctionJdbcDao.deleteByPageId(pageId);
		
		List<AttachedWidget> widgets = pageModel.getWidgets();
		// 插入部件
		if(!widgets.isEmpty()) {
			List<PageWidgetAttrValue> properties = new ArrayList<>();
			widgets.forEach(widget -> {
				widget.getProperties()
					.stream()
					// 如果属性值为空，则不需要存储
					// 但是在查询时，也要返回值为空的属性信息
					.filter(prop -> prop.getValue() != null)
					.forEach(prop -> {
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
		
		// 插入数据
		List<PageDataItem> allData = pageModel.getData();
		if(allData != null && !allData.isEmpty()) {
			pageDataJdbcDao.batchSave(pageId, allData);
		}
		
		// 插入函数
		List<PageEventHandler> handlers = pageModel.getFunctions();
		if(handlers != null && !handlers.isEmpty()) {
			List<PageFunction> funcs = new ArrayList<>();
			List<PageFunctionNode> nodes = new ArrayList<>();
			List<PageFunctionNodePort> ports = new ArrayList<>();
			List<PageFunctionConnection> connections = new ArrayList<>();
			
			handlers.forEach(handler -> {
				PageFunction func = new PageFunction();
				func.setId(handler.getId());
				func.setPageId(pageId);
				funcs.add(func);
				
				handler.getNodes().forEach(visualNode -> {
					PageFunctionNode node = new PageFunctionNode();
					node.setPageId(pageId);
					node.setId(visualNode.getId());
					node.setFunctionId(handler.getId());
					node.setLeft(visualNode.getLeft());
					node.setTop(visualNode.getTop());
					node.setLayout(NodeLayout.fromKey(visualNode.getLayout()));
					node.setCategory(NodeCategory.fromKey(visualNode.getCategory()));
					node.setDataItemId(visualNode.getDataItemId());
					// 函数定义不需要设置 bind_source、api_repo_id 和 code
					nodes.add(node);
					
					InputSequencePort isp = visualNode.getInputSequencePort();
					if(isp != null) {
						PageFunctionNodePort port = new PageFunctionNodePort();
						port.setPageId(pageId);
						port.setId(isp.getId());
						port.setNodeId(visualNode.getId());
						port.setPortType(PortType.SEQUENCE);
						port.setFlowType(FlowType.INPUT);
						ports.add(port);
					}
					visualNode.getOutputSequencePorts().forEach(osp -> {
						PageFunctionNodePort port = new PageFunctionNodePort();
						port.setPageId(pageId);
						port.setId(osp.getId());
						port.setNodeId(visualNode.getId());
						port.setPortType(PortType.SEQUENCE);
						port.setFlowType(FlowType.OUTPUT);
						port.setOutputSequencePortText(osp.getText());
						ports.add(port);
					});
					visualNode.getInputDataPorts().forEach(idp -> {
						PageFunctionNodePort port = new PageFunctionNodePort();
						port.setPageId(pageId);
						port.setId(idp.getId());
						port.setNodeId(visualNode.getId());
						port.setPortType(PortType.DATA);
						port.setFlowType(FlowType.INPUT);
						port.setInputDataPortValue(idp.getValue());
						// TODO: code
						ports.add(port);
					});
					
					visualNode.getOutputDataPorts().forEach(odp -> {
						PageFunctionNodePort port = new PageFunctionNodePort();
						port.setPageId(pageId);
						port.setId(odp.getId());
						port.setNodeId(visualNode.getId());
						port.setPortType(PortType.DATA);
						port.setFlowType(FlowType.OUTPUT);
						// TODO: code
						ports.add(port);
					});
				});
				
				// 连接
				handler.getSequenceConnections().forEach(sc -> {
					PageFunctionConnection conn = new PageFunctionConnection();
					conn.setPageId(pageId);
					conn.setId(sc.getId());
					conn.setFunctionId(handler.getId());
					conn.setFromNodeId(sc.getFromNode());
					conn.setFromOutputPortId(sc.getFromOutput());
					conn.setToNodeId(sc.getToNode());
					conn.setToInputPortId(sc.getToInput());
					connections.add(conn);
				});

				handler.getDataConnections().forEach(dc -> {
					PageFunctionConnection conn = new PageFunctionConnection();
					conn.setPageId(pageId);
					conn.setId(dc.getId());
					conn.setFunctionId(handler.getId());
					conn.setFromNodeId(dc.getFromNode());
					conn.setFromOutputPortId(dc.getFromOutput());
					conn.setToNodeId(dc.getToNode());
					conn.setToInputPortId(dc.getToInput());
					connections.add(conn);
				});
			});
			// 批量插入页面函数
			pageFunctionJdbcDao.batchSave(funcs);
			// 批量插入函数节点
			pageFunctionNodeJdbcDao.batchSave(nodes);
			// 批量插入函数节点的端口
			pageFunctionNodePortJdbcDao.batchSave(ports);
			// 批量插入端口连接
			pageFunctionConnectionJdbcDao.batchSave(connections);
		}
	}

	private void updatePageFileInGit(Project project, ProjectResource projectResource, PageModel pageModel) {
		// 确保这是一个页面
		if(!projectResource.isPage()) {
			logger.warn("往 git 仓库中更新页面模型失败：不是一个有效的页面");
			return;
		}
		
		UserInfo user = userService.findById(project.getCreateUserId()).orElse(null);
		if(user == null) {
			logger.warn("往 git 仓库中更新页面模型失败：未找到项目创建者的用户信息");
			return;
		}
		
		Integer parentResourceId = projectResource.getParentId();
		String relativeDir = parentResourceId == Constant.TREE_ROOT_ID ? null: String.join("/", this.findParentPathes(parentResourceId));
		
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).map(rootDir -> {
			return new ProjectContext(user.getLoginName(), project.getName(), rootDir).getGitRepositoryDirectory();
		}).ifPresent(rootPath -> {
			Path path = rootPath;
			if(StringUtils.isNotBlank(relativeDir)) {
				path = path.resolve(relativeDir);
			}
			
			path = path.resolve(projectResource.getFileName());
			try {
				ObjectMapper mapper = new ObjectMapper();
				String json = mapper.writeValueAsString(pageModel);
				Files.writeString(path, json);
			} catch (IOException e) {
				logger.error("为页面生成 json文件时出错！", e);
			}
			
		});
	}
	
	// TODO: 此处需要性能优化
	@Override
	public PageModel getPageModel(Integer projectId, Integer pageId) {
		PageModel model = new PageModel();
		
		model.setPageId(pageId);
		
		List<AttachedWidget> widgets = getPageWidgets(projectId, pageId);
		model.setWidgets(widgets);
		
		List<PageDataItem> pageData = getPageData(pageId);
		model.setData(pageData);
		
		List<PageEventHandler> functions;
		// 因为事件处理函数是与部件上的事件绑定的，所以这里加一层判断
		if(widgets.isEmpty()) {
			functions = Collections.emptyList();
		}else {
			// 获取页面中所有事件
			List<AttachedWidgetProperty> events = widgets.stream().flatMap(widget -> {
				return widget.getProperties().stream().filter(prop -> prop.getValueType().equals(WidgetPropertyValueType.FUNCTION.getKey()));
			}).collect(Collectors.toList());
			functions = getPageFunctions(pageId, events, pageData);
		}
		model.setFunctions(functions);
		
		return model;
	}

	private List<AttachedWidget> getPageWidgets(Integer projectId, Integer pageId) {
		List<PageWidget> pageWidgets = pageWidgetDao.findAllByPageIdOrderBySeq(pageId);
		
		if(pageWidgets.isEmpty()) {
			return Collections.emptyList();
		}
		
		Map<Integer, List<ApiWidget>> cachedAndGroupedWidgets = new HashMap<>();
		// 以下逻辑是用来支持版本升级的
		
		// 如果页面模型中存在部件，则获取项目依赖的所有部件列表
		// 然后根据这个列表来匹配
		projectDependenceService
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
					// FIXME: 已从表中删除了 name 字段
					// result.setApiRepoName(apiRepo.getName());
					result.setApiRepoId(apiRepo.getId());
					result.setCategory(apiRepo.getCategory());
				});
				return result;
			})
			.filter(apiVersionInfo -> apiVersionInfo.getCategory() == RepoCategory.WIDGET)
			.forEach(apiVersionInfo -> {
				// 4. 获取到该版本下的所有部件
				List<ApiWidget> widgets = apiComponentDao.findAllByApiRepoVersionId(apiVersionInfo.getApiRepoVersionId());
				cachedAndGroupedWidgets.put(apiVersionInfo.getApiRepoId(), widgets);
			});
		
		return pageWidgets.stream().map(item -> {
			AttachedWidget result = new AttachedWidget();
			result.setId(item.getId());
			result.setParentId(item.getParentId());
			result.setWidgetCode(item.getWidgetCode());
			result.setApiRepoId(item.getApiRepoId());
			
			// 5. 部件的属性，延迟加载，只有页面中使用到了部件，才加载其属性
			// 注意，如果一个部件在页面中使用了多次，最好只加载一次属性，可使用缓存优化
			// FIXME: 当没有为项目添加依赖时，会报 null 异常
			cachedAndGroupedWidgets.get(item.getApiRepoId())
				.stream()
				.filter(component -> component.getCode().equals(item.getWidgetCode()))
				.findFirst()
				.ifPresent(component -> {
					// 因为页面设计器中需要根据 widgetName 来定位部件实例，所以不能使用 label
					result.setWidgetName(component.getName());
					result.setWidgetId(component.getId());

					List<PageWidgetAttrValue> attachedProperties = pageWidgetAttrValueDao.findAllByPageWidgetId(item.getId());
					
					List<AttachedWidgetProperty> properties = apiComponentAttrDao
							.findAllByApiWidgetIdOrderByCode(component.getId())
							.stream()
							.map(componentAttr -> {
								// 注意，属性列表要先获取部件的属性列表，然后再赋值，确保新增的属性（页面模型中未添加）也能包括进来
								// 部件属性基本信息
								AttachedWidgetProperty property = new AttachedWidgetProperty();
								property.setCode(componentAttr.getCode());
								
								// name 只能取 name，不能取 label
								property.setName(componentAttr.getName());
								property.setValueType(componentAttr.getValueType().getKey());
								// 如果属性为事件，则添加事件参数
								if(componentAttr.getValueType() == WidgetPropertyValueType.FUNCTION) {
									// 加载参数的定义
									List<ApiWidgetEventArg> args = apiComponentAttrFunArgDao.findAllByApiWidgetPropertyId(componentAttr.getId());
									List<EventArgument> eventArgs = args.stream().map(arg -> {
										EventArgument ea = new EventArgument();
										ea.setCode(arg.getCode());
										ea.setName(arg.getName());
										ea.setLabel(arg.getLabel());
										ea.setValueType(arg.getValueType().getKey());
										ea.setDefaultValue(arg.getDefaultValue());
										ea.setDescription(arg.getDescription());
										return ea;
									}).collect(Collectors.toList());
									property.setEventArgs(eventArgs);
								}
								
								// 以下设置部件属性的实例信息
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
	}
	
	private List<PageDataItem> getPageData(Integer pageId) {
		return pageDataDao.findAllByPageId(pageId);
	}
	
	private List<PageEventHandler> getPageFunctions(Integer pageId, List<AttachedWidgetProperty> events, List<PageDataItem> pageData) {
		// 尽量减少查询次数
		List<PageFunction> functions = pageFunctionDao.findAllByPageId(pageId);
		List<PageFunctionNode> nodes = functions.isEmpty() ? Collections.emptyList() : pageFunctionNodeDao.findAllByPageId(pageId);
		List<PageFunctionNodePort> ports = nodes.isEmpty() ? Collections.emptyList() : pageFunctionNodePortDao.findAllByPageId(pageId);
		List<PageFunctionConnection> connections = ports.isEmpty() ? Collections.emptyList() : pageFunctionConnectionDao.findAllByPageId(pageId);
		
		return functions.stream().map(func -> {
			PageEventHandler handler = new PageEventHandler();
			
			handler.setId(func.getId());
			
			// 获取对应的事件定义，因为肯定会存在对应的事件定义，所以这里直接使用 get()
			AttachedWidgetProperty event = events.stream().filter(e -> e.getValue().equals(func.getId())).findAny().get();
			
			// 过滤出当前函数中的节点
			List<VisualNode> visualNodes = nodes.stream().filter(node -> node.getFunctionId().equals(func.getId())).map(node -> {
				VisualNode visualNode = new VisualNode();
				visualNode.setId(node.getId());
				visualNode.setLeft(node.getLeft());
				visualNode.setTop(node.getTop());
				
				String dataItemId = node.getDataItemId();
				visualNode.setDataItemId(dataItemId);
				
				Optional<PageDataItem> refDataItemOption = pageData
					.stream()
					.filter(dataItem -> dataItem.getId().equals(dataItemId))
					.findAny();
				
				// 如果是函数定义，则从事件定义中获取相关信息
				if(node.getCategory() == NodeCategory.FUNCTION) {
					visualNode.setCaption("事件处理函数");
					visualNode.setText(event.getName());
				} else if(node.getCategory() == NodeCategory.VARIABLE_SET) {
					refDataItemOption
						.ifPresentOrElse(dataItem -> visualNode.setCaption("Set " + dataItem.getName()), ()->{
							// TODO: 打印错误日志
						});
				} else if(node.getCategory() == NodeCategory.VARIABLE_GET) {
					refDataItemOption
						.ifPresentOrElse(dataItem -> visualNode.setCaption("Get " + dataItem.getName()), ()->{
							// TODO: 打印错误日志
						});
				}
				
				visualNode.setLayout(node.getLayout().getKey());
				visualNode.setCategory(node.getCategory().getKey());
				
				ports.stream().filter(port -> port.getNodeId().equals(node.getId())).forEach(port -> {
					if(port.getPortType() == PortType.SEQUENCE) {
						if(port.getFlowType() == FlowType.INPUT) {
							// input sequence port
							InputSequencePort isp = new InputSequencePort();
							isp.setId(port.getId());
							visualNode.setInputSequencePort(isp);
						} else if(port.getFlowType() == FlowType.OUTPUT) {
							// output sequence port
							OutputSequencePort osp = new OutputSequencePort();
							osp.setId(port.getId());
							osp.setText(port.getOutputSequencePortText());
							visualNode.addOutputSequencePort(osp);
						}
					}else if(port.getPortType() == PortType.DATA) {
						if(port.getFlowType() == FlowType.INPUT) {
							// input data port
							InputDataPort idp = new InputDataPort();
							idp.setId(port.getId());
							if(node.getCategory() == NodeCategory.VARIABLE_SET) {
								idp.setName("set"); // 固定值
								// 获取变量定义中的类型信息
								refDataItemOption.ifPresent(dataItem -> idp.setType(dataItem.getType()));
								idp.setValue(port.getInputDataPortValue());
							}
							visualNode.addInputDataPort(idp);
						} else if(port.getFlowType() == FlowType.OUTPUT) {
							// output data port
							if(node.getCategory() == NodeCategory.FUNCTION) {
								event.getEventArgs().forEach(arg -> {
									DataPort odp = new DataPort();
									odp.setId(port.getId());
									odp.setName(arg.getName());
									odp.setType(arg.getValueType());
									visualNode.addOutputDataPort(odp);
								});
							}else if(node.getCategory() == NodeCategory.VARIABLE_GET) {
								// variable get 节点只有一个 output data port
								DataPort odp = new DataPort();
								odp.setId(port.getId());
								odp.setName("value");
								refDataItemOption.ifPresent(dataItem -> odp.setType(dataItem.getType()));
								
								visualNode.addOutputDataPort(odp);
							}
						}
					}
				});
				
				return visualNode;
			}).collect(Collectors.toList());
			handler.setNodes(visualNodes);
			
			List<NodeConnection> dataConnections = new ArrayList<NodeConnection>();
			List<NodeConnection> sequenceConnections = new ArrayList<NodeConnection>();
			// 过滤出当前函数的连接
			connections.stream().filter(conn -> conn.getFunctionId().equals(func.getId())).forEach(conn -> {
				NodeConnection nodeConnection = new NodeConnection();
				nodeConnection.setId(conn.getId());
				nodeConnection.setFromNode(conn.getFromNodeId());
				nodeConnection.setFromOutput(conn.getFromOutputPortId());
				nodeConnection.setToNode(conn.getToNodeId());
				nodeConnection.setToInput(conn.getToInputPortId());
				
				String fromNodeId = conn.getFromNodeId();
				String fromOutputId = conn.getFromOutputPortId();
				// 找到节点 -> 然后从节点中找到端口 -> 判断端口的类型 -> 将连接分为序列连接和数据连接
				visualNodes.stream().filter(visualNode -> visualNode.getId().equals(fromNodeId)).findAny().ifPresent(visualNode -> {
					if(visualNode.getOutputSequencePorts().stream().anyMatch(osp -> fromOutputId.equals(osp.getId()))) {
						sequenceConnections.add(nodeConnection);
					}else {
						dataConnections.add(nodeConnection);
					}
				});
			});
			
			handler.setSequenceConnections(sequenceConnections);
			handler.setDataConnections(dataConnections);
			
			return handler;
		}).collect(Collectors.toList());
	}
	
	@Override
	public PageModel createPageModelWithStdPage(Integer pageId) {
		PageModel pageModel = new PageModel();
		pageModel.setPageId(pageId);
		
		// 标准库所实现的 API 仓库的地址
		String stdApiRepoUrl = propertyService.findStringValue(CmPropKey.STD_WIDGET_API_GIT_URL, "");
		Integer stdApiRepoPublishUserId = propertyService.findIntegerValue(CmPropKey.STD_WIDGET_REGISTER_USERID, 1);
		String rootWidgetName = propertyService.findStringValue(CmPropKey.STD_WIDGET_ROOT_NAME, "Page");
		
		apiRepoDao.findByGitRepoUrlAndCreateUserId(stdApiRepoUrl, stdApiRepoPublishUserId).map(apiRepo -> {
			AttachedWidget rootWidget = new AttachedWidget();
			rootWidget.setApiRepoId(apiRepo.getId());
			return rootWidget;
		}).map(rootWidget -> {
			apiRepoVersionService.findLatestVersion(rootWidget.getApiRepoId()).ifPresent(apiVersion -> {
				apiComponentDao.findByApiRepoVersionIdAndNameIgnoreCase(apiVersion.getId(), rootWidgetName).ifPresent(apiComponent -> {
					rootWidget.setWidgetCode(apiComponent.getCode());
					rootWidget.setWidgetId(apiComponent.getId());
					rootWidget.setWidgetName(apiComponent.getName());
				});
			});
			return rootWidget;
		}).map(rootWidget -> {
			rootWidget.setId(IdGenerator.uuid());
			rootWidget.setParentId(Constant.TREE_ROOT_ID.toString());
			
			List<AttachedWidgetProperty> rootWidgetProperties = apiComponentAttrDao
					.findAllByApiWidgetIdOrderByCode(rootWidget.getWidgetId())
					.stream()
					.map(apiComponentAttr -> {
						AttachedWidgetProperty p = new AttachedWidgetProperty();
						p.setId(IdGenerator.uuid());
						p.setValue(apiComponentAttr.getDefaultValue());
						
						p.setCode(apiComponentAttr.getCode());
						p.setName(apiComponentAttr.getName());
						p.setValueType(apiComponentAttr.getValueType().getKey());
						p.setExpr(false);
						return p;
					})
					.collect(Collectors.toList());
			rootWidget.setProperties(rootWidgetProperties);
			return rootWidget;
		}).ifPresentOrElse(rootWidget -> {
			pageModel.setWidgets(Collections.singletonList(rootWidget));
			this.updatePageModel(pageModel);
		}, () -> {
			logger.error("从标准库中没有找到 Page 部件。请 BlockLang 管理员确认是否有往组件市场中注册标准库！");
			pageModel.setWidgets(Collections.emptyList());
		});
		return pageModel;
	}

}
