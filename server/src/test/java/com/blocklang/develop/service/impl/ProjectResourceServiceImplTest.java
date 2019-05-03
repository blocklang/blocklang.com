package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
	public void insert_if_not_set_seq() {
		Integer projectId = Integer.MAX_VALUE;
		
		Project project = new Project();
		project.setName("project");
		project.setCreateUserName("jack");
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setAppType(AppType.WEB);
		resource.setKey("key");
		resource.setName("name");
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		
		Integer id = projectResourceService.insert(project, resource).getId();
		
		Optional<ProjectResource> resourceOption = projectResourceDao.findById(id);
		assertThat(resourceOption).isPresent();
		assertThat(resourceOption.get().getSeq()).isEqualTo(1);
	}
	
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
	public void find_children_name_is_null_then_set_name_with_key() {
		Integer projectId = Integer.MAX_VALUE;
		
		Project project = new Project();
		project.setCreateUserName("jack");
		project.setName("my-project");
		project.setId(projectId);
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName(null);
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource).getId();
		
		List<ProjectResource> resources = projectResourceService.findChildren(project, Constant.TREE_ROOT_ID);
		assertThat(resources).hasSize(1);
		assertThat(resources.get(0).getName()).isEqualTo("key1");
	}
	
	@Test
	public void find_children_at_sub_group() {
		Integer projectId = Integer.MAX_VALUE;
		
		Project project = new Project();
		project.setCreateUserName("jack");
		project.setName("my-project");
		project.setId(projectId);
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
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
		resource.setProjectId(projectId);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		List<ProjectResource> resources = projectResourceService.findChildren(project, savedResourceId);
		assertThat(resources).hasSize(1);
		assertThat(resources.get(0).getKey()).isEqualTo("key2");
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
		assertThat(projectResourceService.findByKey(
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
		
		assertThat(projectResourceService.findByKey(
				projectId, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.PAGE, 
				AppType.WEB,
				"key1")).isPresent();
	}
	
	@Test
	public void find_by_name_at_root_no_data() {
		assertThat(projectResourceService.findByName(
				Integer.MAX_VALUE, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.PAGE, 
				AppType.WEB,
				"not-exist-name")).isEmpty();
	}
	
	@Test
	public void find_by_name_at_root_success() {
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
		
		assertThat(projectResourceService.findByName(
				projectId, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.PAGE, 
				AppType.WEB,
				"name1")).isPresent();
	}

	@Test
	public void find_parent_groups_by_parent_path_is_not_exist() {
		assertThat(projectResourceService.findParentGroupsByParentPath(null, null)).isEmpty();
		assertThat(projectResourceService.findParentGroupsByParentPath(null, "")).isEmpty();
	}
	
	@Test
	public void find_parent_id_by_parent_path_is_root_path() {
		assertThat(projectResourceService.findParentGroupsByParentPath(1, null)).isEmpty();
		assertThat(projectResourceService.findParentGroupsByParentPath(1, "")).isEmpty();
	}
	
	@Test
	public void find_parent_id_by_parent_path_is_one_level() {
		Integer projectId = Integer.MAX_VALUE;
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer resourceId = projectResourceDao.save(resource).getId();
		
		List<ProjectResource> groups = projectResourceService.findParentGroupsByParentPath(projectId, "key1");
		assertThat(groups).hasSize(1);
		assertThat(groups.get(0).getId()).isEqualTo(resourceId);
	}
	
	@Test
	public void find_parent_id_by_parent_path_is_two_level_success() {
		Integer projectId = Integer.MAX_VALUE;
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer resourceId = projectResourceDao.save(resource).getId();
		
		resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(resourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer resourceId2 = projectResourceDao.save(resource).getId();
		
		List<ProjectResource> groups = projectResourceService.findParentGroupsByParentPath(projectId, "key1/key2");
		assertThat(groups).hasSize(2);
		assertThat(groups.get(0).getId()).isEqualTo(resourceId);
		assertThat(groups.get(1).getId()).isEqualTo(resourceId2);
	}

	@Test
	public void find_parent_id_by_parent_path_is_two_level_not_exist() {
		Integer projectId = Integer.MAX_VALUE;
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource).getId();
		
		assertThat(projectResourceService.findParentGroupsByParentPath(projectId, "key1/key2")).isEmpty();
	}
}
