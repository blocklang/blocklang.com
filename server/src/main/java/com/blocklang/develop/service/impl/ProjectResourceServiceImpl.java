package com.blocklang.develop.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.constant.Constant;
import com.blocklang.core.constant.GitFileStatus;
import com.blocklang.core.git.GitFileInfo;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.service.PropertyService;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.dao.ProjectResourceDao;
import com.blocklang.develop.data.UncommittedFile;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectResourceService;

@Service
public class ProjectResourceServiceImpl implements ProjectResourceService {

	private static final Logger logger = LoggerFactory.getLogger(ProjectResourceServiceImpl.class);
	
	@Autowired
	private ProjectResourceDao projectResourceDao;
	@Autowired
	private PropertyService propertyService;
	
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
		Map<String, GitFileInfo> fileMap = files.stream().collect(Collectors.toMap(GitFileInfo::getName, Function.identity()));
		
		Map<String, GitFileStatus> fileStatusMap = projectContext
				.map(context -> GitUtils.status(context.getGitRepositoryDirectory(), relativeDir))
				.orElse(Collections.emptyMap());
		
		result.forEach(resource -> {
			if(StringUtils.isBlank(resource.getName())) {
				resource.setName(resource.getKey());
			}
			GitFileInfo fileInfo = fileMap.get(resource.getFileName());
			if(fileInfo != null) {
				resource.setLatestCommitId(fileInfo.getCommitId());
				resource.setLatestCommitTime(fileInfo.getLatestCommitTime());
				resource.setLatestShortMessage(fileInfo.getLatestShortMessage());
				resource.setLatestFullMessage(fileInfo.getLatestFullMessage());
			}
			
			if(resource.isGroup()) {
				// 当文件夹未跟踪时，则下面的子文件夹不会再在查询结果中，但也可以归为未跟踪。
				// 所以，如果找不到当前文件夹的状态，则继承父文件夹的状态
				String path = null;
				if(StringUtils.isBlank(relativeDir)) {
					path = resource.getKey();
				} else {
					path = relativeDir + "/" + resource.getKey();
				}
				GitFileStatus status = fileStatusMap.get(path);
				if(status == null) {
					status = fileStatusMap.get(relativeDir);
				}
				resource.setGitStatus(status);
			} else {
				String path = null;
				if(StringUtils.isBlank(relativeDir)) {
					path = resource.getFileName();
				} else {
					path = relativeDir + "/" + resource.getFileName();
				}
				GitFileStatus status = fileStatusMap.get(path);
				resource.setGitStatus(status);
			}
			
		});
		
		return result;
	}

	@Override
	public String findParentPath(Integer resourceId) {
		List<String> pathes = new ArrayList<String>();
		
		while(resourceId != Constant.TREE_ROOT_ID) {
			resourceId = projectResourceDao.findById(resourceId).map(resource -> {
				pathes.add(0, resource.getKey());
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
		if(projectId == null) {
			return Collections.emptyList();
		}
		if(StringUtils.isBlank(parentPath)) {
			return Collections.emptyList();
		}
		
		List<ProjectResource> result = new ArrayList<ProjectResource>();
		
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
			return Collections.emptyList();
		}
		
		return result;
	}

	@Override
	public List<UncommittedFile> findChanges(Project project) {
		return Collections.emptyList();
	}

}
