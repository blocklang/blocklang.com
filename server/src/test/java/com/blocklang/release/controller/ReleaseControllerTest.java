package com.blocklang.release.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.blocklang.develop.model.Project;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.release.data.NewReleaseParam;
import com.blocklang.release.model.ProjectTag;
import com.blocklang.release.service.GitService;
import com.blocklang.release.service.ProjectBuildService;
import com.blocklang.release.service.ProjectTagService;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(ReleaseController.class)
public class ReleaseControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private ProjectService projectService;
	
	@MockBean
	private ProjectTagService projectTagService;
	
	@MockBean
	private ProjectBuildService projectBuildService;
	
	@MockBean
	private GitService gitService;
	
	@Before
	public void setUp() {
		RestAssuredMockMvc.mockMvc(mvc);
	}
	
	@Test
	public void post_release_param_is_blank() {
		NewReleaseParam release = new NewReleaseParam();
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.version", hasItems("版本不能为空"), 
				  "errors.name", hasItems("发行版的名称不能为空"));
	}
	
	@Test
	public void post_release_project_not_exist() {
		NewReleaseParam release = prepareNewParam();
		
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void post_release_version_is_used() {
		NewReleaseParam release = prepareNewParam();
		
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setProjectName("demo_project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectTag projectTag = new ProjectTag();
		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.of(projectTag));
		
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.version", hasItems("版本号 0.1.0 已被占用"));
	}
	
	@Test
	public void post_release_git_tag_failed() {
		NewReleaseParam release = prepareNewParam();
		
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setProjectName("demo_project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.empty());
		
		when(gitService.tag(any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("为 Git 仓库添加附注标签失败"));
	}
	
	@Test
	public void post_release_git_tag_success() {
		NewReleaseParam release = prepareNewParam();
		
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setProjectName("demo_project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.empty());
		
		String tagId = "i-am-git-tag-object-id";
		when(gitService.tag(any())).thenReturn(Optional.of(tagId));
		
		ProjectTag projectTag = new ProjectTag();
		projectTag.setId(1);
		when(projectTagService.save(any())).thenReturn(projectTag);
		
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project");
		
		verify(projectTagService).save(any());
		verify(projectBuildService).save(any());
	}

	private NewReleaseParam prepareNewParam() {
		NewReleaseParam release = new NewReleaseParam();
		release.setVersion("0.1.0");
		release.setName("发行版名称");
		release.setDescription("发行版描述");
		return release;
	}
}
