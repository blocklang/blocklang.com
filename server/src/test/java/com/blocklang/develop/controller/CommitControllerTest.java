package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.data.UncommittedFile;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.service.ProjectAuthorizationService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;

import io.restassured.http.ContentType;

@WebMvcTest(CommitController.class)
public class CommitControllerTest extends AbstractControllerTest{

	@MockBean
	private ProjectService projectService;
	@MockBean
	private ProjectAuthorizationService projectAuthorizationService;
	@MockBean
	private ProjectResourceService projectResourceService;

	@Test
	public void list_changes_project_not_found() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void list_changes_anonymous_can_access_public_project() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(true);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("[]"));
	}
	
	@WithMockUser(username = "other")
	@Test
	public void list_changes_login_user_can_access_public_project() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(true);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("[]"));
	}
	
	@Test
	public void list_changes_anonymous_can_not_access_private_project() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(false);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void list_changes_login_user_can_not_access_private_project() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(false);
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void list_changes_login_user_can_read_private_project() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(false);
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		when(projectResourceService.findChanges(project)).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("[]"));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void list_changes_no_changes_can_write_private_project() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(false);
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		when(projectResourceService.findChanges(project)).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("[]"));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void list_changes_no_changes_can_admin_private_project() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(false);
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		when(projectResourceService.findChanges(project)).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("[]"));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void list_changes_no_changes_forbidden_access_private_project() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(false);
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.FORBIDDEN);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		when(projectResourceService.findChanges(project)).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void list_changes_has_changes() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(true);
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		List<UncommittedFile> changes = new ArrayList<UncommittedFile>();
		UncommittedFile file1 = new UncommittedFile();
		changes.add(file1);
		when(projectResourceService.findChanges(project)).thenReturn(changes);
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(1));
	}
	
	@Test
	public void stage_changes_anonymous_can_not_stage() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.post("/projects/{owner}/{projectName}/stage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void stage_changes_project_not_found() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.post("/projects/{owner}/{projectName}/stage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void stage_changes_login_user_has_no_auth() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(false);
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.post("/projects/{owner}/{projectName}/stage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void stage_changes_login_user_can_read() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(false);
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.post("/projects/{owner}/{projectName}/stage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void stage_changes_login_user_can_write() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(false);
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.post("/projects/{owner}/{projectName}/stage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK);
		
		verify(projectResourceService).stageChanges(any(), any());
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void stage_changes_login_user_can_admin() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(false);
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.post("/projects/{owner}/{projectName}/stage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK);
		
		verify(projectResourceService).stageChanges(any(), any());
	}
}
