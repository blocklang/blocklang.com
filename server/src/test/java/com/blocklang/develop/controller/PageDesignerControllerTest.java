package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.util.NestedServletException;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.designer.data.PageModel;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.develop.service.ProjectPermissionService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.RepoType;
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
	private ProjectDependenceService projectDependenceService;
	@MockBean
	private ProjectResourceService projectResourceService;
	@MockBean
	private ProjectPermissionService projectPermissionService;
	
	@Test
	public void listProjectDependences_only_support_repo_is_ide() {
		Exception exception = Assertions.assertThrows(NestedServletException.class, () -> given()
				.contentType(ContentType.JSON)
				.when()
					.get("/designer/projects/{projectId}/dependences?repo=api", 1));
		
		assertThat(exception.getMessage()).endsWith("当前仅支持获取 ide 依赖。");
	}
	
	@Test
	public void listProjectDependences_project_not_exist() {
		when(projectService.findById(anyInt())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?repo=ide", 1)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void listProjectDependences_project_exist_but_can_not_read() {
		Project project = new Project();
		project.setId(1);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?repo=ide", 1)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@DisplayName("只过滤出 IDE 版仓库，即排除掉 PROD 版仓库")
	@Test
	public void listProjectDependences_only_filter_ide_repo() {
		Project project = new Project();
		project.setId(1);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		when(propertyService.findStringValue(eq(CmPropKey.STD_WIDGET_IDE_GIT_URL))).thenReturn(Optional.of("url"));
		
		ProjectDependence dependence = new ProjectDependence();
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(1);
		componentRepo.setGitRepoUrl("url2");
		componentRepo.setGitRepoWebsite("website1");
		componentRepo.setGitRepoOwner("owner1");
		componentRepo.setGitRepoName("repoName1");
		componentRepo.setCategory(RepoCategory.WIDGET);
		componentRepo.setRepoType(RepoType.PROD);

		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setVersion("0.0.1");
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setId(2);
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		ProjectDependenceData data = new ProjectDependenceData(dependence, componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
		when(projectDependenceService.findProjectDependences(anyInt(), anyBoolean())).thenReturn(Collections.singletonList(data));

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?repo=ide", 1)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(0));
	}
	
	@DisplayName("成功过滤出 IDE 版仓库")
	@Test
	public void listProjectDependences_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		when(propertyService.findStringValue(eq(CmPropKey.STD_WIDGET_IDE_GIT_URL))).thenReturn(Optional.of("url"));
		
		ProjectDependence dependence = new ProjectDependence();
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(1);
		componentRepo.setGitRepoUrl("url");
		componentRepo.setGitRepoWebsite("website1");
		componentRepo.setGitRepoOwner("owner1");
		componentRepo.setGitRepoName("repoName1");
		componentRepo.setCategory(RepoCategory.WIDGET);
		componentRepo.setRepoType(RepoType.IDE);
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setVersion("0.0.1");
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setId(2);
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		ProjectDependenceData data = new ProjectDependenceData(dependence, componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
		when(projectDependenceService.findProjectDependences(anyInt(), anyBoolean())).thenReturn(Collections.singletonList(data));

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?repo=ide", 1)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(1),
					"[0].id", equalTo(1),
					"[0].gitRepoWebsite", equalTo("website1"),
					"[0].gitRepoOwner", equalTo("owner1"),
					"[0].gitRepoName", equalTo("repoName1"),
					"[0].apiRepoId", equalTo(2),
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
	
	@Test
	public void get_project_dependeces_widgets_can_not_read_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences/widgets", 1)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@Test
	public void get_project_dependeces_widgets_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		when(projectDependenceService.findAllWidgets(anyInt())).thenReturn(Collections.emptyList());
		
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
	
	@Test
	public void get_page_model_can_not_read_project() {
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
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}

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
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(projectResourceService.getPageModel(anyInt(), anyInt())).thenReturn(new PageModel());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("data.size()", is(0),
					"widgets.size()", is(0),
					"functions.size()", is(0));
	}

	@Test
	public void update_page_model_anonymous_can_not_update() {
		PageModel model = new PageModel();
		
		given()
			.contentType(ContentType.JSON)
			.body(model)
		.when()
			.put("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void update_page_model_page_not_exist() {
		PageModel model = new PageModel();
		model.setPageId(1);
		
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(model)
		.when()
			.put("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void update_page_model_invalid_page() {
		PageModel model = new PageModel();
		
		ProjectResource page = new ProjectResource();
		page.setId(1);
		page.setProjectId(1);
		page.setResourceType(ProjectResourceType.GROUP);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		given()
			.contentType(ContentType.JSON)
			.body(model)
		.when()
			.put("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void update_page_model_project_not_exist() {
		PageModel model = new PageModel();
		
		ProjectResource page = new ProjectResource();
		page.setId(1);
		page.setProjectId(1);
		page.setResourceType(ProjectResourceType.PAGE);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		when(projectService.findById(anyInt())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(model)
		.when()
			.put("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void update_page_model_has_write_permission_success() {
		Map<String, Object> model = new HashMap<String, Object>();
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectResource page = new ProjectResource();
		page.setProjectId(1);
		page.setResourceType(ProjectResourceType.PAGE);
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(page));
		
		Project project = new Project();
		project.setId(1);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		given()
			.contentType(ContentType.JSON)
			.body(model)
		.when()
			.put("/designer/pages/{pageId}/model", "1")
		.then()
			.statusCode(HttpStatus.SC_NO_CONTENT)
			.body(equalTo(""));
		
		verify(projectResourceService).updatePageModel(any(), any(), any());
	}

	@Test
	public void get_asset_file_is_forbidden() throws IOException {
		given()
			.contentType(ContentType.JSON)
			.header("referer", "not_null")
		.when()
			.get("/designer/assets/{gitRepoWebsite}/{gitRepoOwner}/{gitRepoName}/{version}/{fileName}", "a", "b", "c", "d", "a.js")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_asset_file_not_exist(@TempDir Path dataRootDirectory) throws IOException {
		when(propertyService.findStringValue(anyString(), anyString())).thenReturn(dataRootDirectory.toString());
		
		given()
			.contentType(ContentType.JSON)
			.header("referer", "not_null")
		.when()
			.get("/designer/assets/{gitRepoWebsite}/{gitRepoOwner}/{gitRepoName}/{version}/{fileName}", "a", "b", "c", "d", "e.js")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_asset_js_file_success(@TempDir Path dataRootDirectory) throws IOException {
		when(propertyService.findStringValue(anyString(), anyString())).thenReturn(dataRootDirectory.toString());
		
		Path dir = dataRootDirectory.resolve("marketplace").resolve("a").resolve("b").resolve("c").resolve("package").resolve("d");
		Path createdDir = Files.createDirectories(dir);
		Files.writeString(createdDir.resolve("main.bundle.js"), "a js file");
		
		given()
			.contentType(ContentType.JSON)
			.header("referer", "not_null")
		.when()
			.get("/designer/assets/{gitRepoWebsite}/{gitRepoOwner}/{gitRepoName}/{version}/{fileName}", "a", "b", "c", "d", "main.bundle.js")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.contentType("application/javascript")
			.body(equalTo("a js file"));
	}
	
	@Test
	public void get_asset_css_file_success(@TempDir Path dataRootDirectory) throws IOException {
		when(propertyService.findStringValue(anyString(), anyString())).thenReturn(dataRootDirectory.toString());
		
		Path dir = dataRootDirectory.resolve("marketplace").resolve("a").resolve("b").resolve("c").resolve("package").resolve("d");
		Path createdDir = Files.createDirectories(dir);
		Files.writeString(createdDir.resolve("main.bundle.css"), "a css file");
		
		given()
			.contentType(ContentType.JSON)
			.header("referer", "not_null")
		.when()
			.get("/designer/assets/{gitRepoWebsite}/{gitRepoOwner}/{gitRepoName}/{version}/{fileName}", "a", "b", "c", "d", "main.bundle.css")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.contentType("text/css")
			.body(equalTo("a css file"));
	}
	
	@Test
	public void get_asset_map_file_success(@TempDir Path dataRootDirectory) throws IOException {
		when(propertyService.findStringValue(anyString(), anyString())).thenReturn(dataRootDirectory.toString());
		
		Path dir = dataRootDirectory.resolve("marketplace").resolve("a").resolve("b").resolve("c").resolve("package").resolve("d");
		Path createdDir = Files.createDirectories(dir);
		Files.writeString(createdDir.resolve("main.bundle.js.map"), "a js source map file");
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/assets/{gitRepoWebsite}/{gitRepoOwner}/{gitRepoName}/{version}/{fileName}", "a", "b", "c", "d", "main.bundle.js.map")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.contentType("application/octet-stream")
			.body(equalTo("a js source map file"));
	}
	
	@Test
	public void get_asset_svg_file_success(@TempDir Path dataRootDirectory) throws IOException {
		when(propertyService.findStringValue(anyString(), anyString())).thenReturn(dataRootDirectory.toString());
		
		Path dir = dataRootDirectory.resolve("marketplace").resolve("a").resolve("b").resolve("c").resolve("package").resolve("d");
		Path createdDir = Files.createDirectories(dir);
		Files.writeString(createdDir.resolve("icons.svg"), "a js source map file");
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/assets/{gitRepoWebsite}/{gitRepoOwner}/{gitRepoName}/{version}/{fileName}", "a", "b", "c", "d", "icons.svg")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.contentType("image/svg+xml")
			.body(equalTo("a js source map file"));
	}
}
