package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.git.exception.GitEmptyCommitException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.data.CommitMessage;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.service.ProjectPermissionService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;

import io.restassured.http.ContentType;

@WebMvcTest(CommitController.class)
public class CommitControllerTest extends AbstractControllerTest{

	@MockBean
	private ProjectService projectService;
	@MockBean
	private ProjectResourceService projectResourceService;
	@MockBean
	private ProjectPermissionService projectPermissionService;

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
	public void list_changes_can_not_read_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void list_changes_success() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(true);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("[]"));
	}
	
	@Test
	public void stage_changes_anonymous_can_not_stage() {
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/stage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void stage_changes_project_not_found() {
		String[] param = new String[] {};
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/stage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void stage_changes_can_not_write_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/stage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void stage_changes_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/stage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK);
		
		verify(projectResourceService).stageChanges(any(), any());
	}

	@Test
	public void unstage_changes_anonymous_can_not_unstage() {
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/unstage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void unstage_changes_project_not_found() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/unstage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void unstage_changes_can_not_write_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/unstage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void unstage_changes_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/unstage-changes", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK);
		
		verify(projectResourceService).unstageChanges(any(), any());
	}

	@Test
	public void commit_anonymous_can_not_commit() {
		CommitMessage param = new CommitMessage();
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/commits", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void commit_message_can_not_blank() {		
		CommitMessage param = new CommitMessage();
		param.setValue("");

		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/commits", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.value", hasItem("提交信息不能为空"),
					"errors.value.size()", is(1));
	}

	@WithMockUser(username = "jack")
	@Test
	public void commit_project_not_found() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		CommitMessage param = new CommitMessage();
		param.setValue("first commit");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/commits", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser(username = "other")
	@Test
	public void commit_can_not_write_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());

		CommitMessage param = new CommitMessage();
		param.setValue("first commit");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/commits", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}

	@WithMockUser(username = "jack")
	@Test
	public void commit_when_no_changed_files() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		CommitMessage param = new CommitMessage();
		param.setValue("first commit");
		
		when(projectResourceService.commit(any(), any(), anyString())).thenThrow(GitEmptyCommitException.class);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/commits", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItem("没有发现变更的文件"),
					"errors.globalErrors.size()", is(1));
		
		verify(projectResourceService).commit(any(), any(), anyString());
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void commit_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		CommitMessage param = new CommitMessage();
		param.setValue("first commit");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/commits", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK);
		
		verify(projectResourceService).commit(any(), any(), anyString());
	}

}
