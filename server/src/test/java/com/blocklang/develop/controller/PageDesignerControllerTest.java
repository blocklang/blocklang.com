package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.designer.data.PageModel;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectAuthorizationService;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

import io.restassured.http.ContentType;

@WebMvcTest(PageDesignerController.class)
public class PageDesignerControllerTest extends AbstractControllerTest {

	@MockBean
	private ProjectService projectService;
	@MockBean
	private ProjectAuthorizationService projectAuthorizationService;
	@MockBean
	private ProjectDependenceService projectDependenceService;
	@MockBean
	private ProjectResourceService projectResourceService;
	
	@Test
	public void list_project_dependences_project_not_exist() {
		when(projectService.findById(anyInt())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?category=dev", 1)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	// 用户未登录，所以无权访问私有项目
	@Test
	public void list_project_dependences_project_exist_but_can_not_read() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?category=dev", 1)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void list_project_dependences_only_filter_dev_repo() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		ProjectDependence dependence = new ProjectDependence();
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(1);
		componentRepo.setGitRepoWebsite("website1");
		componentRepo.setGitRepoOwner("owner1");
		componentRepo.setGitRepoName("repoName1");
		componentRepo.setName("name1");
		componentRepo.setCategory(RepoCategory.WIDGET);
		componentRepo.setStd(true);
		componentRepo.setIsIdeExtension(false);
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setVersion("0.0.1");
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setId(2);
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		ProjectDependenceData data = new ProjectDependenceData(dependence, componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
		when(projectDependenceService.findProjectDependences(anyInt())).thenReturn(Collections.singletonList(data));

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?category=dev", 1)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(0));
	}
	
	@Test
	public void list_project_dependences_success() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		ProjectDependence dependence = new ProjectDependence();
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(1);
		componentRepo.setGitRepoWebsite("website1");
		componentRepo.setGitRepoOwner("owner1");
		componentRepo.setGitRepoName("repoName1");
		componentRepo.setName("name1");
		componentRepo.setCategory(RepoCategory.WIDGET);
		componentRepo.setStd(true);
		componentRepo.setIsIdeExtension(true);
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setVersion("0.0.1");
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setId(2);
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		ProjectDependenceData data = new ProjectDependenceData(dependence, componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
		when(projectDependenceService.findProjectDependences(anyInt())).thenReturn(Collections.singletonList(data));

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?category=dev", 1)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(1),
					"[0].id", equalTo(1),
					"[0].gitRepoWebsite", equalTo("website1"),
					"[0].gitRepoOwner", equalTo("owner1"),
					"[0].gitRepoName", equalTo("repoName1"),
					"[0].name", equalTo("name1"),
					"[0].apiRepoId", equalTo(2),
					"[0].category", equalTo("Widget"),
					"[0].version", equalTo("0.0.1"),
					"[0].std", equalTo(true));
	}
	
	@Test
	public void get_project_dependeces_widgets_project_not_exist() {
		when(projectService.findById(anyInt())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences/widgets", 1)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	// 匿名用户不能访问私有项目
	@Test
	public void get_project_dependeces_widgets_anonymous_user_forbidden_access_private_project() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences/widgets", 1)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	// 匿名用户能访问公开项目
	@Test
	public void get_project_dependeces_widgets_anonymous_user_can_access_public_project() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		when(projectDependenceService.findAllWidgets(anyInt())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences/widgets", 1)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("[]"));
	}

	// 登录用户能访问公开项目
	@WithMockUser("jack")
	@Test
	public void get_project_dependeces_widgets_login_user_can_access_public_project() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		when(projectDependenceService.findAllWidgets(anyInt())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences/widgets", 1)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("[]"));
	}

	// 登录用户不能访问没有 read | write | admin 权限的私有项目
	@WithMockUser("jack")
	@Test
	public void get_project_dependeces_widgets_login_user_can_not_access_private_project_that_has_no_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences/widgets", 1)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	// 登录用户能访问有 read | write | admin 权限的私有项目
	@WithMockUser("jack")
	@Test
	public void get_project_dependeces_widgets_login_user_can_not_access_private_project_that_has_read_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences/widgets", 1)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("[]"));
	}

	@Test
	public void get_page_model_page_not_found() {
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_page_model_page_invalid_page() {
		ProjectResource page = new ProjectResource();
		page.setId(1);
		page.setResourceType(ProjectResourceType.GROUP/*not PAGE*/);
		
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_page_model_project_not_exist() {
		ProjectResource page = new ProjectResource();
		page.setId(1);
		page.setProjectId(11);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		when(projectService.findById(anyInt())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	// 匿名用户能访问公开项目
	@Test
	public void get_page_model_anonymous_user_can_access_public_project() {
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectResource page = new ProjectResource();
		page.setId(1);
		page.setProjectId(1);
		page.setResourceType(ProjectResourceType.PAGE);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		when(projectResourceService.getPageModel(anyInt(), anyInt())).thenReturn(new PageModel());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	// 匿名用户不能访问私有项目
	@Test
	public void get_page_model_anonymous_user_can_access_private_project() {
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectResource page = new ProjectResource();
		page.setProjectId(1);
		page.setResourceType(ProjectResourceType.PAGE);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	// 登录用户能访问公开项目
	@Test
	public void get_page_model_login_user_can_access_public_project() {
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectResource page = new ProjectResource();
		page.setId(1);
		page.setProjectId(1);
		page.setResourceType(ProjectResourceType.PAGE);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		when(projectResourceService.getPageModel(anyInt(), anyInt())).thenReturn(new PageModel());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	// 登录用户不能访问没有权限的私有项目
	@WithMockUser("jack")
	@Test
	public void get_page_model_login_user_forbidden_no_permission_private_project() {
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectResource page = new ProjectResource();
		page.setProjectId(1);
		page.setResourceType(ProjectResourceType.PAGE);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	// 登录用户能访问有读权限的私有项目
	@WithMockUser("jack")
	@Test
	public void get_page_model_login_user_can_access_has_read_permission_private_project() {
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectResource page = new ProjectResource();
		page.setId(1);
		page.setProjectId(1);
		page.setResourceType(ProjectResourceType.PAGE);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		when(projectResourceService.getPageModel(anyInt(), anyInt())).thenReturn(new PageModel());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	// 登录用户能访问有写权限的私有项目
	@WithMockUser("jack")
	@Test
	public void get_page_model_login_user_can_access_has_write_permission_private_project() {
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectResource page = new ProjectResource();
		page.setId(1);
		page.setProjectId(1);
		page.setResourceType(ProjectResourceType.PAGE);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		when(projectResourceService.getPageModel(anyInt(), anyInt())).thenReturn(new PageModel());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	// 登录用户能访问有管理权限的私有项目
	@WithMockUser("jack")
	@Test
	public void get_page_model_login_user_can_access_has_admin_permission_private_project() {
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectResource page = new ProjectResource();
		page.setId(1);
		page.setProjectId(1);
		page.setResourceType(ProjectResourceType.PAGE);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		PageModel model = new PageModel();
		when(projectResourceService.getPageModel(anyInt(), anyInt())).thenReturn(model);
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}

	@Test
	public void update_page_model_forbidden_anonymous_user() {
		Map<String, Object> model = new HashMap<String, Object>();
		
		given()
			.contentType(ContentType.JSON)
			.body(model)
		.when()
			.put("/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void update_page_model_page_not_found() {
		Map<String, Object> model = new HashMap<String, Object>();
		
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(model)
		.when()
			.put("/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void update_page_model_login_user_has_no_permission() {
		Map<String, Object> model = new HashMap<String, Object>();
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectResource page = new ProjectResource();
		page.setProjectId(1);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		Project project = new Project();
		project.setId(1);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		given()
			.contentType(ContentType.JSON)
			.body(model)
		.when()
			.put("/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void update_page_model_login_user_has_read_permission() {
		Map<String, Object> model = new HashMap<String, Object>();
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectResource page = new ProjectResource();
		page.setProjectId(1);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		Project project = new Project();
		project.setId(1);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		given()
			.contentType(ContentType.JSON)
			.body(model)
		.when()
			.put("/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void update_page_model_success() {
		Map<String, Object> model = new HashMap<String, Object>();
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectResource page = new ProjectResource();
		page.setProjectId(1);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		Project project = new Project();
		project.setId(1);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		given()
			.contentType(ContentType.JSON)
			.body(model)
		.when()
			.put("/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_CREATED);
		
		verify(projectResourceService).updatePageModel(any());
	}

}
