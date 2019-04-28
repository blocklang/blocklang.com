package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.dao.ProjectResourceDao;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectResourceService;

public class ProjectResourceServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ProjectResourceService projectResourceService;
	
	@Autowired
	private ProjectResourceDao projectResourceDao;
	
	@Autowired
	private MessageSource messageSource;
	
	@Test
	public void find_children_no_data() {
		List<ProjectResource> resources = projectResourceService.findChildren(null, 9999);
		assertThat(resources).isEmpty();
	}
	
	@Test
	public void find_children_at_root_has_two_project_success() {
		Project project = new Project();
		project.setCreateUserName("jack");
		project.setName("my-project");
		project.setId(1);
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
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
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		List<ProjectResource> resources = projectResourceService.findChildren(project, Constant.TREE_ROOT_ID);
		assertThat(resources).hasSize(1);
	}
	
	@Test
	public void find_children_at_root_has_one_project_success() {
		Project project = new Project();
		project.setCreateUserName("jack");
		project.setName("my-project");
		project.setId(1);
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
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
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		List<ProjectResource> resources = projectResourceService.findChildren(project, Constant.TREE_ROOT_ID);
		assertThat(resources).hasSize(1);
	}
	
	@Test
	public void find_parent_path_at_root() {
		String path = projectResourceService.findParentPath(-1);
		assertThat(path).isEmpty();
	}
	
	@Test
	public void find_parent_path_success() {
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
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
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		savedResourceId = projectResourceDao.save(resource).getId();
		
		resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key3");
		resource.setName("name3");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		savedResourceId = projectResourceDao.save(resource).getId();
		
		String path = projectResourceService.findParentPath(savedResourceId);
		assertThat(path).isEqualTo("key1/key2/key3");
	}
	
	// 因为 getTitle 方法用到了 spring 的国际化帮助类，因为需要注入，所以将测试类放在 service 中
	@Test
	public void get_title_main() {
		ProjectResource resource = new ProjectResource();
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setKey(ProjectResource.MAIN_KEY);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("首页");
	}
	
	@Test
	public void get_title_page() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("页面");
	}
	
	@Test
	public void get_title_group() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("分组");
	}
	
	@Test
	public void get_title_templet() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PAGE_TEMPLET);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("模板");
	}
	
	@Test
	public void get_title_readme() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.FILE);
		resource.setKey(ProjectResource.README_KEY);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("README");
	}
	
	@Test
	public void get_title_service() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.SERVICE);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("服务");
	}

	@Test
	public void find_by_id_no_data() {
		assertThat(projectResourceService.findById(Integer.MAX_VALUE)).isEmpty();
	}
	
	@Test
	public void find_by_id_success() {
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedResourceId = projectResourceDao.save(resource).getId();
		
		assertThat(projectResourceService.findById(savedResourceId)).isPresent();
	}
	
	@Test
	public void find_by_key_at_root_no_data() {
		assertThat(projectResourceService.find(
				Integer.MAX_VALUE, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.PAGE, 
				AppType.WEB,
				"not-exist-key")).isEmpty();
	}
	
	@Test
	public void find_by_key_at_root_success() {
		Integer projectId = Integer.MAX_VALUE;
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		assertThat(projectResourceService.find(
				projectId, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.PAGE, 
				AppType.WEB,
				"key1")).isPresent();
	}
}
