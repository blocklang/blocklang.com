package com.blocklang.develop.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.constant.Constant;
import com.blocklang.core.git.GitFileInfo;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.service.PropertyService;
import com.blocklang.develop.dao.ProjectResourceDao;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectResourceService;

@Service
public class ProjectResourceServiceImpl implements ProjectResourceService {

	@Autowired
	private ProjectResourceDao projectResourceDao;
	@Autowired
	private PropertyService propertyService;
	
	//@Transactional
	@Override
	public ProjectResource insert(ProjectResource resource) {
		return projectResourceDao.save(resource);
	}

	@Override
	public List<ProjectResource> findChildren(Project project, int parentResourceId) {
		if(project == null) {
			return new ArrayList<ProjectResource>();
		}
		
		List<GitFileInfo> files = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).map(rootDir -> {
			String relativeDir = parentResourceId == Constant.TREE_ROOT_ID ? null: "";
			ProjectContext context = new ProjectContext(project.getCreateUserName(), project.getName(), rootDir);
			return GitUtils.getFiles(context.getGitRepositoryDirectory(), relativeDir);
		}).orElse(new ArrayList<GitFileInfo>());
		
		// 根据文件名关联
		Map<String, GitFileInfo> fileMap = files.stream().collect(Collectors.toMap(GitFileInfo::getName, Function.identity()));
		
		List<ProjectResource> result = projectResourceDao.findByProjectIdAndParentIdOrderByResourceTypeAscSeqAsc(project.getId(), parentResourceId);
		
		result.forEach(resource -> {
			GitFileInfo fileInfo = fileMap.get(resource.getFileName());
			if(fileInfo != null) {
				resource.setLatestCommitId(fileInfo.getCommitId());
				resource.setLatestCommitTime(fileInfo.getLatestCommitTime());
				resource.setLatestShortMessage(fileInfo.getLatestShortMessage());
				resource.setLatestFullMessage(fileInfo.getLatestFullMessage());
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

}
