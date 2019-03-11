package com.blocklang.release.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.release.service.AppReleaseService;

import io.restassured.http.ContentType;

@WebMvcTest(AppController.class)
public class AppControllerTest extends AbstractControllerTest{
	
	// 因为 config 中的 githubLoginService 没有创建 bean，所以这里 mock 一个
	@MockBean
	private GithubLoginService githubLoginService;
		
	@MockBean
	private AppReleaseService appReleaseService;

	@Test
	public void get_jdks_success() {
		when(appReleaseService.findByAppName(anyString())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/apps/jdk/releases")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(0));
	}
}
