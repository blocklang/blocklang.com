package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.dao.ProjectResourceDao;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectResourceService;

public class ProjectResourceServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ProjectResourceService projectResourceService;
	
	@Autowired
	private ProjectResourceDao projectResourceDao;
	
	@Test
	public void find_children_no_data() {
		List<ProjectResource> resources = projectResourceService.findChildren(8888, 9999);
		assertThat(resources).isEmpty();
	}
	
	@Test
	public void find_children_has_two_project_success() {
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.FUNCTION);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		resource = new ProjectResource();
		resource.setProjectId(2);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.FUNCTION);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		List<ProjectResource> resources = projectResourceService.findChildren(1, Constant.TREE_ROOT_ID);
		assertThat(resources).hasSize(1);
	}
	
	@Test
	public void find_children_has_one_project_success() {
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.FUNCTION);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedResourceId = projectResourceDao.save(resource).getId();
		
		resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.FUNCTION);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		List<ProjectResource> resources = projectResourceService.findChildren(1, Constant.TREE_ROOT_ID);
		assertThat(resources).hasSize(1);
	}
	
}
