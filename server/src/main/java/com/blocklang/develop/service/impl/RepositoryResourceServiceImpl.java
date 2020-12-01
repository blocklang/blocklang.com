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
import com.blocklang.core.util.JsonUtil;
import com.blocklang.core.util.StreamUtil;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.FlowType;
import com.blocklang.develop.constant.NodeCategory;
import com.blocklang.develop.constant.NodeLayout;
import com.blocklang.develop.constant.PortType;
import com.blocklang.develop.constant.RepositoryResourceType;
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
import com.blocklang.develop.dao.RepositoryCommitDao;
import com.blocklang.develop.dao.RepositoryResourceDao;
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
import com.blocklang.develop.designer.data.PageInfo;
import com.blocklang.develop.designer.data.PageModel;
import com.blocklang.develop.designer.data.VisualNode;
import com.blocklang.develop.model.PageDataItem;
import com.blocklang.develop.model.PageFunction;
import com.blocklang.develop.model.PageFunctionConnection;
import com.blocklang.develop.model.PageFunctionNode;
import com.blocklang.develop.model.PageFunctionNodePort;
import com.blocklang.develop.model.PageWidget;
import com.blocklang.develop.model.PageWidgetAttrValue;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryCommit;
import com.blocklang.develop.model.RepositoryContext;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.ProjectDependencyService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.WidgetPropertyValueType;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ApiWidgetDao;
import com.blocklang.marketplace.dao.ApiWidgetEventArgDao;
import com.blocklang.marketplace.dao.ApiWidgetPropertyDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiWidget;
import com.blocklang.marketplace.model.ApiWidgetEventArg;
import com.blocklang.marketplace.service.ApiRepoVersionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 项目资源管理的业务逻辑接口
 * 
 * @author Zhengwei Jin
 *
 */
@Service
public class RepositoryResourceServiceImpl implements RepositoryResourceService {

	private static final Logger logger = LoggerFactory.getLogger(RepositoryResourceServiceImpl.class);
	
	@Autowired
	private RepositoryResourceDao repositoryResourceDao;
	@Autowired
	private RepositoryCommitDao repositoryCommitDao;
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
	private ProjectDependencyService projectDependencyService;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiWidgetDao apiWidgetDao;
	@Autowired
	private ApiRepoVersionService apiRepoVersionService;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiWidgetPropertyDao apiWidgetPropertyDao;
	@Autowired
	private ApiWidgetEventArgDao apiWidgetEventArgDao;
	
	//@Transactional
	@Override
	public RepositoryResource insert(Repository repository, RepositoryResource resource) {
		if(resource.getSeq() == null) {
			Integer nextSeq = repositoryResourceDao.findFirstByRepositoryIdAndParentIdOrderBySeqDesc(resource.getRepositoryId(), resource.getParentId()).map(item -> item.getSeq() + 1).orElse(1);
			resource.setSeq(nextSeq);
		}
		RepositoryResource result = repositoryResourceDao.save(resource);
		PageModel pageModel = this.createPageModelWithStdPage(result);
		
		// 在 git 仓库中添加文件
		Integer parentResourceId = resource.getParentId();
		String relativeDir = parentResourceId == Constant.TREE_ROOT_ID ? null: String.join("/", this.findParentPathes(parentResourceId));
		
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).map(rootDir -> {
			return new RepositoryContext(repository.getCreateUserName(), repository.getName(), rootDir).getGitRepositoryDirectory();
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
			
			// TODO: 是不是需要增加保存 ProjectCommit 功能
			
		});
		
		return result;
	}

	@Override
	public List<RepositoryResource> findChildren(Repository project, Integer parentResourceId) {
		if(project == null) {
			return new ArrayList<RepositoryResource>();
		}
		
		List<RepositoryResource> result = repositoryResourceDao.findByRepositoryIdAndParentIdOrderByResourceTypeAscSeqAsc(project.getId(), parentResourceId);
		if(result.isEmpty()) {
			return new ArrayList<RepositoryResource>();
		}
		
		String relativeDir = parentResourceId == Constant.TREE_ROOT_ID ? "": String.join("/", this.findParentPathes(parentResourceId));
		
		Optional<RepositoryContext> RepositoryContext = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).map(rootDir -> {
			return new RepositoryContext(project.getCreateUserName(), project.getName(), rootDir);
		});
				
		List<GitFileInfo> files = RepositoryContext
				.map(context -> GitUtils.getFiles(context.getGitRepositoryDirectory(), relativeDir))
				.orElse(new ArrayList<GitFileInfo>());
		// 根据文件名关联
		// fileMap 的 key 不包含父目录
		Map<String, GitFileInfo> fileMap = files.stream().collect(Collectors.toMap(GitFileInfo::getName, Function.identity()));
		// fileStatusMap 的 key 中包含父目录，所有父目录
		Map<String, GitFileStatus> fileStatusMap = RepositoryContext
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
			resourceId = repositoryResourceDao.findById(resourceId).map(resource -> {
				if(resource.isDependency()) {
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
	public Optional<RepositoryResource> findByKey(
			Integer projectId, 
			Integer parentId, 
			RepositoryResourceType resourceType,
			AppType appType,
			String key) {
		return repositoryResourceDao.findByRepositoryIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
				projectId, 
				parentId, 
				resourceType,
				appType,
				key);
	}
	
	@Override
	public Optional<RepositoryResource> findByName(
			Integer projectId, 
			Integer parentId, 
			RepositoryResourceType resourceType,
			AppType appType, 
			String name) {
		return repositoryResourceDao.findByRepositoryIdAndParentIdAndResourceTypeAndAppTypeAndNameIgnoreCase(
				projectId, 
				parentId, 
				resourceType,
				appType,
				name);
	}
	
	@Override
	public List<RepositoryResource> findAllPages(Integer projectId, AppType appType) {
		return repositoryResourceDao.findAllByRepositoryIdAndAppTypeAndResourceType(projectId, appType, RepositoryResourceType.PAGE);
	}

	@Override
	public Optional<RepositoryResource> findById(Integer resourceId) {
		return repositoryResourceDao.findById(resourceId);
	}

	@Override
	public List<RepositoryResource> findParentGroupsByParentPath(Integer repositoryId, String parentPath) {
		List<RepositoryResource> result = new ArrayList<RepositoryResource>();
		
		if(repositoryId == null) {
			return result;
		}
		if(StringUtils.isBlank(parentPath)) {
			return result;
		}
		
		String[] keys = parentPath.trim().split("/");
		Integer parentId = Constant.TREE_ROOT_ID;
		boolean allMatched = true;
		for(int i = 0; i < keys.length; i++) {
			String key = keys[i];
			Optional<RepositoryResource> resourceOption;
			if(i == 0) {
				// 只支持在仓库的根目录创建 Project，不支持创建 Group
				resourceOption = repositoryResourceDao.findByRepositoryIdAndParentIdAndResourceTypeAndKeyIgnoreCase(
						repositoryId, 
						parentId, 
						RepositoryResourceType.PROJECT, 
						key);
			} else {
				resourceOption = repositoryResourceDao.findByRepositoryIdAndParentIdAndResourceTypeAndKeyIgnoreCase(
						repositoryId, 
						parentId, 
						RepositoryResourceType.GROUP, 
						key);
			}
			if(resourceOption.isEmpty()) {
				allMatched = false;
				break;
			}
			result.add(resourceOption.get());
			parentId = resourceOption.get().getId();
		}
		
		if(!allMatched) {
			return new ArrayList<RepositoryResource>();
		}
		
		return result;
	}
	
	private List<RepositoryResource> findParentGroupsByParentPath(List<RepositoryResource> projectResources, String parentPath) {
		if(projectResources.isEmpty()) {
			return Collections.emptyList();
		}

		List<RepositoryResource> result = new ArrayList<RepositoryResource>();
		
		String[] keys = parentPath.trim().split("/");
		Integer parentId = Constant.TREE_ROOT_ID;
		boolean allMatched = true;
		for(String key : keys) {
			Integer finalParentId = parentId;
			Optional<RepositoryResource> resourceOption = projectResources.stream()
					.filter(item -> item.getParentId().equals(finalParentId)
							&& item.getResourceType().equals(RepositoryResourceType.GROUP)
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
	public List<UncommittedFile> findChanges(Repository project) {
		Map<String, GitFileStatus> fileStatusMap = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)
				.map(rootDir -> new RepositoryContext(project.getCreateUserName(), project.getName(), rootDir))
				.map(context -> GitUtils.status(context.getGitRepositoryDirectory(), ""))
				.orElse(Collections.emptyMap());
		
		final List<RepositoryResource> allResources = fileStatusMap.isEmpty() ? Collections.emptyList() : repositoryResourceDao.findAllByRepositoryId(project.getId());
		
		return fileStatusMap.entrySet().stream().filter(item -> {
			String filePath = item.getKey();
			// 排除分组
			if(filePath.endsWith(".page.web.json")) {
				return true;
			}
			if(filePath.equalsIgnoreCase(RepositoryResource.README_NAME)) {
				return true;
			}
			if(filePath.equalsIgnoreCase(RepositoryResource.DEPENDENCY_NAME)) {
				return true;
			}
			
			// 到此处，都当成是分组
			return false;
		}).map(item -> {
			// 根据文件名反推资源信息
			String filePath = item.getKey();
			
			RepositoryResourceType resourceType = null;
			AppType appType = null;
			String resourceKey = null;
			String parentPath = null;
			if(filePath.endsWith(".page.web.json")) {
				resourceType = RepositoryResourceType.PAGE;
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
			} else if(filePath.equalsIgnoreCase(RepositoryResource.README_NAME)){
				// README.md 文件只存在于根目录
				resourceType = RepositoryResourceType.FILE;
				appType = AppType.UNKNOWN;
				resourceKey = RepositoryResource.README_KEY;
				parentPath = "";
			} else if(filePath.equalsIgnoreCase(RepositoryResource.DEPENDENCY_NAME)) {
				// DEPENDENCY.json 文件只存在于根目录
				resourceType = RepositoryResourceType.DEPENDENCY;
				appType = AppType.UNKNOWN;
				resourceKey = RepositoryResource.DEPENDENCY_KEY;
				parentPath = "";
			}
			
			Integer parentId = Constant.TREE_ROOT_ID;
			String parentNamePath = "";
			if(StringUtils.isNotBlank(parentPath)) {
				List<RepositoryResource> parentGroups = findParentGroupsByParentPath(allResources, parentPath);
				if(parentGroups.isEmpty()) {
					logger.error("在 git 仓库中存在 " + parentPath + "，但是在资源表中没有找到");
					return null;
				}
				parentId = parentGroups.get(parentGroups.size() - 1).getId();
				for(RepositoryResource each : parentGroups) {
					parentNamePath += (StringUtils.isBlank(each.getName()) ? each.getKey() : each.getName()) + "/";
				}
			}

			Integer finalParentId = parentId;
			String finalResourceKey = resourceKey;
			RepositoryResourceType finalResourceType = resourceType;
			AppType finalAppType = appType;
			
			// 找到资源文件
			Optional<RepositoryResource> resourceOption = allResources
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
			RepositoryResource resource = resourceOption.get();
			file.setIcon(resource.getIcon());
			file.setFullKeyPath(filePath);
			file.setGitStatus(item.getValue());
			file.setResourceName(StringUtils.isBlank(resource.getName()) ? resource.getKey() : resource.getName());
			file.setParentNamePath(parentNamePath);

			return file;
		}).collect(Collectors.toList());
	}

	@Override
	public void stageChanges(Repository project, String[] filePathes) {
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)
		.ifPresent(rootDir -> {
			RepositoryContext context = new RepositoryContext(project.getCreateUserName(), project.getName(), rootDir);
			GitUtils.add(context.getGitRepositoryDirectory(), filePathes);
		});
	}

	@Override
	public void unstageChanges(Repository project, String[] filePathes) {
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)
		.ifPresent(rootDir -> {
			RepositoryContext context = new RepositoryContext(project.getCreateUserName(), project.getName(), rootDir);
			GitUtils.reset(context.getGitRepositoryDirectory(), filePathes);
		});
	}

	@Override
	public String commit(UserInfo user, Repository project, String commitMessage) {
		String commitId = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)
			.map(rootDir -> {
				RepositoryContext context = new RepositoryContext(project.getCreateUserName(), project.getName(), rootDir);
				return GitUtils.commit(context.getGitRepositoryDirectory(), user.getLoginName(), user.getEmail(), commitMessage);
			}).orElse(null);
		
		if(commitId != null) {
			RepositoryCommit commit = new RepositoryCommit();
			commit.setCommitId(commitId);
			commit.setCommitUserId(user.getId());
			commit.setCommitTime(LocalDateTime.now());
			commit.setRepositoryId(project.getId());
			commit.setBranch(Constants.MASTER);
			commit.setShortMessage(commitMessage);
			commit.setCreateUserId(user.getId());
			commit.setCreateTime(LocalDateTime.now());
			
			repositoryCommitDao.save(commit);
		}
		
		return commitId;
	}

	@Override
	public void updatePageModel(Repository repository, RepositoryResource projectResource, PageModel pageModel) {
		this.updatePageModel(pageModel);
		if(repository != null && projectResource != null) {
			this.updatePageFileInGit(repository, projectResource, pageModel);
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

	private void updatePageFileInGit(Repository repository, RepositoryResource projectResource, PageModel pageModel) {
		// 确保这是一个页面
		if(!projectResource.isPage()) {
			logger.warn("往 git 仓库中更新页面模型失败：不是一个有效的页面");
			return;
		}
		
		UserInfo user = userService.findById(repository.getCreateUserId()).orElse(null);
		if(user == null) {
			logger.warn("往 git 仓库中更新页面模型失败：未找到项目创建者的用户信息");
			return;
		}
		
		Integer parentResourceId = projectResource.getParentId();
		String relativeDir = parentResourceId == Constant.TREE_ROOT_ID ? null: String.join("/", this.findParentPathes(parentResourceId));
		
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).map(rootDir -> {
			return new RepositoryContext(user.getLoginName(), repository.getName(), rootDir).getGitRepositoryDirectory();
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
	public PageModel getPageModel(RepositoryResource page) {
		Integer pageId = page.getId();
		PageModel model = new PageModel();
		model.setPageId(pageId);
		
		Optional<RepositoryResource> projectInfoOption = this.findProjectInfo(page);
		if(projectInfoOption.isEmpty()) {
			return model;
		}
		
		List<AttachedWidget> widgets = getPageWidgets(projectInfoOption.get(), pageId);
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
	
	private Optional<RepositoryResource> findProjectInfo(RepositoryResource page) {
		// 如果页面隶属根节点，则不属于任何项目
		if(page.getParentId() == Constant.TREE_ROOT_ID) {
			return Optional.empty();
		}
		
		RepositoryResource result = null;
		do {
			result = repositoryResourceDao.findById(page.getParentId()).orElse(null);
		} while (result != null && !result.isProject());
		
		if(result == null) {
			return Optional.empty();
		}
		
		if(!result.isProject()) {
			return Optional.empty();
		}
		
		return Optional.of(result);
		
	}

	private List<AttachedWidget> getPageWidgets(RepositoryResource project, Integer pageId) {
		List<PageWidget> pageWidgets = pageWidgetDao.findAllByPageIdOrderBySeq(pageId);
		
		if(pageWidgets.isEmpty()) {
			return Collections.emptyList();
		}
		
		Map<Integer, List<ApiWidget>> cachedAndGroupedWidgets = new HashMap<>();

		// 以下逻辑是用来支持版本升级的
		// 如果页面模型中存在部件，则获取项目依赖的所有部件列表
		// 然后根据这个列表来匹配
		projectDependencyService
			// 1. 获取项目的所有依赖
			.findAllDevDependencies(project.getId(), project.getAppType())
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
				List<ApiWidget> widgets = apiWidgetDao.findAllByApiRepoVersionId(apiVersionInfo.getApiRepoVersionId());
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
					
					List<AttachedWidgetProperty> properties = apiWidgetPropertyDao
							.findAllByApiWidgetIdOrderByCode(component.getId())
							.stream()
							.map(componentAttr -> {
								// 注意，属性列表要先获取部件的属性列表，然后再赋值，确保新增的属性（页面模型中未添加）也能包括进来
								// 部件属性基本信息
								AttachedWidgetProperty property = new AttachedWidgetProperty();
								property.setCode(componentAttr.getCode());
								
								// name 只能取 name，不能取 label
								property.setName(componentAttr.getName());
								property.setValueType(componentAttr.getValueType());
								// 如果属性为事件，则添加事件参数
								if(componentAttr.getValueType().equals(WidgetPropertyValueType.FUNCTION.getKey())) {
									// 加载参数的定义
									List<ApiWidgetEventArg> args = apiWidgetEventArgDao.findAllByApiWidgetPropertyId(componentAttr.getId());
									List<EventArgument> eventArgs = args.stream().map(arg -> {
										EventArgument ea = new EventArgument();
										ea.setCode(arg.getCode());
										ea.setName(arg.getName());
										ea.setLabel(arg.getLabel());
										ea.setValueType(arg.getValueType());
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
	public PageModel createPageModelWithStdPage(RepositoryResource page) {
		PageModel pageModel = new PageModel();
		pageModel.setPageId(page.getId());
		
		PageInfo pageInfo = new PageInfo();
		pageInfo.setId(page.getId());
		pageInfo.setKey(page.getKey());
		pageInfo.setLabel(page.getName());
		pageInfo.setResourceType(page.getResourceType().getKey());
		pageModel.setPageInfo(pageInfo);
		
		// 标准库所实现的 API 仓库的地址
		String stdApiRepoUrl = propertyService.findStringValue(CmPropKey.STD_WIDGET_API_GIT_URL, "");
		Integer stdApiRepoPublishUserId = propertyService.findIntegerValue(CmPropKey.STD_REPO_REGISTER_USER_ID, 1);
		String rootWidgetName = propertyService.findStringValue(CmPropKey.STD_WIDGET_ROOT_NAME, "Page");
		
		apiRepoDao.findByGitRepoUrlAndCreateUserId(stdApiRepoUrl, stdApiRepoPublishUserId).map(apiRepo -> {
			AttachedWidget rootWidget = new AttachedWidget();
			rootWidget.setApiRepoId(apiRepo.getId());
			return rootWidget;
		}).map(rootWidget -> {
			apiRepoVersionService.findLatestStableVersion(rootWidget.getApiRepoId()).ifPresent(apiVersion -> {
				apiWidgetDao.findByApiRepoVersionIdAndNameIgnoreCase(apiVersion.getId(), rootWidgetName).ifPresent(apiComponent -> {
					rootWidget.setWidgetCode(apiComponent.getCode());
					rootWidget.setWidgetId(apiComponent.getId());
					rootWidget.setWidgetName(apiComponent.getName());
				});
			});
			return rootWidget;
		}).map(rootWidget -> {
			rootWidget.setId(IdGenerator.uuid());
			rootWidget.setParentId(Constant.TREE_ROOT_ID.toString());
			
			List<AttachedWidgetProperty> rootWidgetProperties = apiWidgetPropertyDao
					.findAllByApiWidgetIdOrderByCode(rootWidget.getWidgetId())
					.stream()
					.map(apiComponentAttr -> {
						AttachedWidgetProperty p = new AttachedWidgetProperty();
						p.setId(IdGenerator.uuid());
						p.setValue(apiComponentAttr.getDefaultValue());
						
						p.setCode(apiComponentAttr.getCode());
						p.setName(apiComponentAttr.getName());
						p.setValueType(apiComponentAttr.getValueType());
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

	/**
	 * 初始化以下资源：
	 * <ul>
	 * <li>Main 页面
	 * <li>DEPENDENCY.json
	 * </ul>
	 */
	@Override
	public RepositoryResource createWebProject(Repository repository, RepositoryResource project) {
		// 创建项目资源
		if(project.getSeq() == null) {
			Integer nextSeq = repositoryResourceDao
					.findFirstByRepositoryIdAndParentIdOrderBySeqDesc(project.getRepositoryId(), project.getParentId())
					.map(item -> item.getSeq() + 1)
					.orElse(1);
			project.setSeq(nextSeq);
		}
		RepositoryResource savedProject = repositoryResourceDao.save(project);
		
		// 生成入口模块：Main 页面
		RepositoryResource mainPage = createMainPageForWebProject(repository, savedProject);
		// 创建空页面，默认为空页面添加根节点，包括 Page 部件及其属性。
		PageModel pageModel = createPageModelWithStdPage(mainPage);
		// 生成 DEPENDENCY.json 文件
		RepositoryResource dependency = createDependencyFile(repository, savedProject);

		// 在 git 仓库中添加文件
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).ifPresent(rootDir -> {
			RepositoryContext context = new RepositoryContext(repository.getCreateUserName(), repository.getName(), rootDir);
			
			Path projectDir = context
					.getGitRepositoryDirectory()
					.resolve(savedProject.getKey());
			
			String mainPageJson = "{}";
			try {
				mainPageJson = JsonUtil.stringify(pageModel);
			} catch (JsonProcessingException e) {
				logger.error("转换 json 失败", e);
			}
			try {
				Files.createDirectory(projectDir);
				Path mainPageFile = projectDir.resolve(mainPage.getFileName());
				Files.writeString(mainPageFile, mainPageJson, StandardOpenOption.CREATE);
			} catch (IOException e) {
				logger.error("为 main 页面生成 json 文件时出错", e);
			}
			
			addProjectDependencyJsonFile(dependency, projectDir);
			
			userService.findById(savedProject.getCreateUserId()).ifPresent(user -> {
				String commitMessage = "Init Web Project";
				String commitId = GitUtils
					.addAllAndCommit(context.getGitRepositoryDirectory(), user.getLoginName(), user.getEmail(), commitMessage);
				
				RepositoryCommit commit = new RepositoryCommit();
				commit.setCommitId(commitId);
				commit.setCommitUserId(user.getId());
				commit.setCommitTime(LocalDateTime.now());
				commit.setRepositoryId(repository.getId());
				commit.setBranch(Constants.MASTER);
				commit.setShortMessage(commitMessage);
				commit.setCreateUserId(user.getId());
				commit.setCreateTime(LocalDateTime.now());
				repositoryCommitDao.save(commit);
			});
		});
		return savedProject;
	}
	
	/**
	 * 生成默认模块: Main 页面
	 */
	private RepositoryResource createMainPageForWebProject(Repository repository, RepositoryResource project) {
		RepositoryResource main = new RepositoryResource();
		main.setRepositoryId(repository.getId());
		main.setKey(RepositoryResource.MAIN_KEY);
		main.setName(RepositoryResource.MAIN_NAME);
		main.setResourceType(RepositoryResourceType.PAGE);
		main.setParentId(project.getId());
		main.setAppType(project.getAppType());
		main.setSeq(1);
		main.setCreateUserId(project.getCreateUserId());
		main.setCreateTime(LocalDateTime.now());
		
		// 因为是空模板，所以这里只引用模板，但没有应用模板
		// 但是因为没有保存，所以此行代码是多余的。
		// 注意，在项目资源表中，不需要保存 templateId
		// 如果不是空模板，则后面直接在页面中应用模板即可
		// TODO: 空模板，也可称为默认模板，空模板中也可能有内容，如只显示“Hello World”，
		// 因此，后续要添加应用模板功能，不要再注释掉此行代码
		// resource.setTempletId(projectResourceService.getEmptyTemplet().getId());
		
		// 此方法中实现了应用模板功能
		// TODO: 是否需要将应用模板逻辑，单独提取出来？
		return repositoryResourceDao.save(main);
	}
	
	private RepositoryResource createAppForMiniProgram(Repository repository, RepositoryResource project) {
		return createApp(repository, project);
	}
	
	private RepositoryResource createAppForHarmonyOSLiteWearableProject(Repository repository, RepositoryResource project) {
		return createApp(repository, project);
	}

	private RepositoryResource createApp(Repository repository, RepositoryResource project) {
		RepositoryResource app = new RepositoryResource();
		
		// TODO: 定义一个 APP 组件，跟 Page 组件类似，但是不能包含子部件
		app.setRepositoryId(repository.getId());
		app.setKey(RepositoryResource.APP_KEY);
		app.setName(RepositoryResource.APP_NAME);
		app.setResourceType(RepositoryResourceType.PAGE);
		app.setParentId(project.getId());
		app.setAppType(project.getAppType());
		app.setDeviceType(project.getDeviceType());
		app.setSeq(1);
		app.setCreateUserId(project.getCreateUserId());
		app.setCreateTime(LocalDateTime.now());
		
		return repositoryResourceDao.save(app);
	}
	
	/**
	 * 往数据库中存储 DEPENDENCY.json 文件基本信息
	 * 
	 * @param repository 仓库基本信息
	 * @param project 项目基本信息
	 * @return DEPENDENCY.json 基本信息
	 */
	private RepositoryResource createDependencyFile(Repository repository, RepositoryResource project) {
		RepositoryResource dependency = new RepositoryResource();
		dependency.setRepositoryId(repository.getId());
		dependency.setKey(RepositoryResource.DEPENDENCY_KEY);
		dependency.setName(RepositoryResource.DEPENDENCY_NAME);
		dependency.setResourceType(RepositoryResourceType.DEPENDENCY);
		dependency.setAppType(project.getAppType());
		dependency.setParentId(project.getId());
		dependency.setSeq(2); // 排在 Main 页面之后
		dependency.setCreateUserId(project.getCreateUserId());
		dependency.setCreateTime(LocalDateTime.now());
		
		return repositoryResourceDao.save(dependency);
	}

	/**
	 * 初始化以下文件：
	 * <ul>
	 * <li>PROJECT.json
	 * <li>DEPENDENCY.json
	 * <li>app (入口)
	 * <li>pages/index 页面
	 * </ul>
	 */
	@Override
	public RepositoryResource createMiniProgram(
			Repository repository, 
			RepositoryResource project,
			ApiRepo apiRepo, 
			ApiWidget appWidget, 
			ApiWidget pageWidget) {
		// 创建项目资源
		if(project.getSeq() == null) {
			Integer nextSeq = repositoryResourceDao
				.findFirstByRepositoryIdAndParentIdOrderBySeqDesc(project.getRepositoryId(), project.getParentId())
				.map(item -> item.getSeq() + 1)
				.orElse(1);
			project.setSeq(nextSeq);
		}
		RepositoryResource savedProject = repositoryResourceDao.save(project);
		
		// 生成入口模块：app
		RepositoryResource app = createAppForMiniProgram(repository, savedProject);
		// 有一个特殊的资源，没有 ui，只需要配置属性。
		// 创建空页面，默认为空页面添加根节点，包括 Page 部件及其属性。
		PageModel appModel = createAppModel(app.getId(), apiRepo, appWidget);
		this.updatePageModel(appModel);
		
		// 生成 DEPENDENCY.json 文件
		RepositoryResource dependency = createDependencyFile(repository, savedProject);

		// 添加 pages/index 页面
		// 1. 先创建 pages 分组
		RepositoryResource pagesDir = new RepositoryResource();
		pagesDir.setRepositoryId(repository.getId());
		pagesDir.setKey("pages");
		pagesDir.setName("pages");
		pagesDir.setResourceType(RepositoryResourceType.GROUP);
		pagesDir.setParentId(savedProject.getId());
		pagesDir.setAppType(project.getAppType());
		pagesDir.setSeq(3);
		pagesDir.setCreateUserId(project.getCreateUserId());
		pagesDir.setCreateTime(LocalDateTime.now());
		repositoryResourceDao.save(pagesDir);
		// 2. 再创建 index 页面
		RepositoryResource indexPage = new RepositoryResource();
		indexPage.setRepositoryId(repository.getId());
		indexPage.setKey("index");
		indexPage.setName("index");
		indexPage.setResourceType(RepositoryResourceType.PAGE);
		indexPage.setParentId(pagesDir.getId());
		indexPage.setAppType(project.getAppType());
		indexPage.setSeq(4);
		indexPage.setCreateUserId(project.getCreateUserId());
		indexPage.setCreateTime(LocalDateTime.now());
		repositoryResourceDao.save(indexPage);
		
		PageModel indexPageModel = createAppModel(app.getId(), apiRepo, pageWidget);
		this.updatePageModel(indexPageModel);

		// 在 git 仓库中添加文件
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).ifPresent(rootDir -> {
			RepositoryContext context = new RepositoryContext(repository.getCreateUserName(), repository.getName(), rootDir);
			
			Path projectDir = context
					.getGitRepositoryDirectory()
					.resolve(savedProject.getKey());
			try {
				Files.createDirectory(projectDir);
			} catch (IOException e) {
				logger.error("创建文件夹时出错", e);
			}
			
			// PROJECT.json
			addProjectJsonFile(savedProject, projectDir);
			// DEPENDENCY.json 文件
			addProjectDependencyJsonFile(dependency, projectDir);
			// app 入口页面
			addAppFile(app, appModel, projectDir);
			// pages/index 页面
			addIndexPageFile(indexPage, indexPageModel, projectDir);
			
			Path gitRootDirectory = context.getGitRepositoryDirectory();
			commitAllToGitRepository(repository, savedProject, gitRootDirectory, "初始化小程序项目");
		});
		return savedProject;
	}
	
	@Override
	public RepositoryResource createHarmonyOSLiteWearableProject(
			Repository repository, 
			RepositoryResource project,
			ApiRepo apiRepo, 
			ApiWidget appWidget, 
			ApiWidget pageWidget) {
		// 创建项目资源
		if(project.getSeq() == null) {
			Integer nextSeq = repositoryResourceDao
				.findFirstByRepositoryIdAndParentIdOrderBySeqDesc(project.getRepositoryId(), project.getParentId())
				.map(item -> item.getSeq() + 1)
				.orElse(1);
			project.setSeq(nextSeq);
		}
		RepositoryResource savedProject = repositoryResourceDao.save(project);
		
		// 生成入口模块：app
		RepositoryResource app = createAppForHarmonyOSLiteWearableProject(repository, savedProject);
		// 有一个特殊的资源，没有 ui，只需要配置属性。
		// 创建空页面，默认为空页面添加根节点，包括 Page 部件及其属性。
		// TODO: 将 APP 部件设计为显示所有属性和事件的样式；或者在其中显示 App 字样和使用说明等
		PageModel appModel = createAppModel(app.getId(), apiRepo, appWidget);
		this.updatePageModel(appModel);
		
		// 生成 DEPENDENCY.json 文件
		RepositoryResource dependency = createDependencyFile(repository, savedProject);

		// 添加 pages/index 页面
		// 1. 先创建 pages 分组
		RepositoryResource pagesDir = new RepositoryResource();
		pagesDir.setRepositoryId(repository.getId());
		pagesDir.setKey("pages");
		pagesDir.setName("pages");
		pagesDir.setResourceType(RepositoryResourceType.GROUP);
		pagesDir.setParentId(savedProject.getId());
		pagesDir.setAppType(project.getAppType());
		pagesDir.setDeviceType(project.getDeviceType());
		pagesDir.setSeq(3);
		pagesDir.setCreateUserId(project.getCreateUserId());
		pagesDir.setCreateTime(LocalDateTime.now());
		repositoryResourceDao.save(pagesDir);
		// 2. 再创建 index 页面
		RepositoryResource indexPage = new RepositoryResource();
		indexPage.setRepositoryId(repository.getId());
		indexPage.setKey("index");
		indexPage.setName("index");
		indexPage.setResourceType(RepositoryResourceType.PAGE);
		indexPage.setParentId(pagesDir.getId());
		indexPage.setAppType(project.getAppType());
		indexPage.setDeviceType(project.getDeviceType());
		indexPage.setSeq(4);
		indexPage.setCreateUserId(project.getCreateUserId());
		indexPage.setCreateTime(LocalDateTime.now());
		repositoryResourceDao.save(indexPage);
		
		PageModel indexPageModel = createAppModel(app.getId(), apiRepo, pageWidget);
		this.updatePageModel(indexPageModel);

		// 在 git 仓库中添加文件
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).ifPresent(rootDir -> {
			RepositoryContext context = new RepositoryContext(repository.getCreateUserName(), repository.getName(), rootDir);
			
			Path projectDir = context
					.getGitRepositoryDirectory()
					.resolve(savedProject.getKey());
			
			try {
				Files.createDirectory(projectDir);
			} catch (IOException e) {
				logger.error("创建文件夹时出错", e);
			}
			
			// PROJECT.json
			addProjectJsonFile(savedProject, projectDir);
			// DEPENDENCY.json 文件
			addProjectDependencyJsonFile(dependency, projectDir);
			// app 入口页面
			addAppFile(app, appModel, projectDir);
			// pages/index 页面
			addIndexPageFile(indexPage, indexPageModel, projectDir);
			
			Path gitRootDirectory = context.getGitRepositoryDirectory();
			commitAllToGitRepository(repository, savedProject, gitRootDirectory, "初始化鸿蒙轻量级智能穿戴项目");
			
			// TODO: commit 之后就触发自动生成代码功能
			
		});
		return savedProject;
	}
	
	/**
	 * 在项目的根目录下新增 PROJECT.json 文件。
	 * 
	 * <p> 注意：PROJECT.json 文件与数据库中的项目基本信息（ResourceType 为 Project 的资源）对应。
	 * 所以不需要在数据库表中再存储 PROJECT.json 文件信息。
	 * 
	 * @param projectInfo 项目基本详细
	 * @param projectDirectory 项目的根目录
	 */
	private void addProjectJsonFile(RepositoryResource projectInfo, Path projectDirectory) {
		Map<String, Object> projectJson = new HashMap<>();
		projectJson.put("id", projectInfo.getId());
		projectJson.put("key", projectInfo.getKey());
		projectJson.put("label", projectInfo.getName());
		projectJson.put("appType", projectInfo.getAppType().getKey());
		projectJson.put("version", Constants.MASTER);
		try {
			String projectJsonString = JsonUtil.stringify(projectJson);
			Path projectJsonFile = projectDirectory.resolve(RepositoryResource.PROJECT_JSON_NAME);
			Files.writeString(projectJsonFile, projectJsonString);
		} catch (IOException e) {
			logger.error("将 PROJECT.json 转换为 json 字符串时出错", e);
		}
	}

	private void addProjectDependencyJsonFile(RepositoryResource dependency, Path projectDirectory) {
		try {
			// TODO: 保存默认依赖的组件库
			String dependencyJson = "{}";
			Path dependencyFile = projectDirectory.resolve(dependency.getFileName());
			Files.writeString(dependencyFile, dependencyJson, StandardOpenOption.CREATE);
		} catch (IOException e) {
			logger.error("生成 DEPENDENCY.json 文件时出错", e);
		}
	}
	
	private void addAppFile(RepositoryResource appInfo, PageModel appModel, Path projectDirectory) {
		// 空页面
		String appJson = "{}";
		try {
			appJson = JsonUtil.stringify(appModel);
		} catch (JsonProcessingException e) {
			logger.error("转换 json 失败", e);
		}
		try {
			Path mainPageFile = projectDirectory.resolve(appInfo.getFileName());
			Files.writeString(mainPageFile, appJson, StandardOpenOption.CREATE);
		} catch (IOException e) {
			logger.error("为 app 生成 json 文件时出错", e);
		}
	}
	
	private void addIndexPageFile(RepositoryResource indexPageInfo, PageModel indexPageModel, Path projectDirectory) {
		String indexPageJson = "{}";
		try {
			indexPageJson = JsonUtil.stringify(indexPageModel);
		} catch (JsonProcessingException e) {
			logger.error("转换 json 失败", e);
		}
		try {
			Path pagesDirPath = projectDirectory.resolve("pages");
			Files.createDirectory(pagesDirPath);
			Path indexPageFile = pagesDirPath.resolve(indexPageInfo.getFileName());
			Files.writeString(indexPageFile, indexPageJson, StandardOpenOption.CREATE);
		} catch (IOException e) {
			logger.error("为 pages/index 生成 json 文件时出错", e);
		}
	}
	
	private void commitAllToGitRepository(Repository repositoryInfo, RepositoryResource project, Path gitRepoRootDirectory, String commitMessage) {
		userService.findById(project.getCreateUserId()).ifPresent(user -> {
			String commitId = GitUtils
				.addAllAndCommit(gitRepoRootDirectory, user.getLoginName(), user.getEmail(), commitMessage);
			
			RepositoryCommit commit = new RepositoryCommit();
			commit.setCommitId(commitId);
			commit.setCommitUserId(user.getId());
			commit.setCommitTime(LocalDateTime.now());
			commit.setRepositoryId(repositoryInfo.getId());
			commit.setBranch(Constants.MASTER);
			commit.setShortMessage(commitMessage);
			commit.setCreateUserId(user.getId());
			commit.setCreateTime(LocalDateTime.now());
			repositoryCommitDao.save(commit);
		});
	}
	
	private PageModel createAppModel(Integer pageId, ApiRepo apiRepo, ApiWidget widget) {
		PageModel pageModel = new PageModel();
		pageModel.setPageId(pageId);

		AttachedWidget rootWidget = new AttachedWidget();
		rootWidget.setId(IdGenerator.uuid());
		rootWidget.setParentId(Constant.TREE_ROOT_ID.toString());
		rootWidget.setApiRepoId(apiRepo.getId());
		rootWidget.setWidgetCode(widget.getCode());
		rootWidget.setWidgetId(widget.getId());
		rootWidget.setWidgetName(widget.getName());

		List<AttachedWidgetProperty> properties = apiWidgetPropertyDao
			.findAllByApiWidgetIdOrderByCode(widget.getId())
			.stream()
			.map(apiWidgetProperty -> {
				AttachedWidgetProperty result = new AttachedWidgetProperty();
				result.setId(IdGenerator.uuid());
				result.setValue(apiWidgetProperty.getDefaultValue());
				result.setCode(apiWidgetProperty.getCode());
				result.setName(apiWidgetProperty.getName());
				result.setValueType(apiWidgetProperty.getValueType());
				result.setExpr(false);
				return result;
			})
			.collect(Collectors.toList());
		rootWidget.setProperties(properties);

		pageModel.setWidgets(Collections.singletonList(rootWidget));

		return pageModel;
	}

	@Override
	public Optional<RepositoryResource> findProject(Integer repositoryId, String projectKey) {
		return repositoryResourceDao.findByRepositoryIdAndParentIdAndResourceTypeAndKeyIgnoreCase(repositoryId, Constant.TREE_ROOT_ID, RepositoryResourceType.PROJECT, projectKey);
	}

	@Override
	public List<RepositoryResource> findAllProject(Integer repositoryId) {
		return repositoryResourceDao.findAllByRepositoryIdAndResourceType(repositoryId, RepositoryResourceType.PROJECT);
	}

}
