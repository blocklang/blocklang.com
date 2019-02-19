package com.blocklang.core.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.test.AbstractControllerTest;

import io.restassured.http.ContentType;

@WebMvcTest(LoggedUserController.class)
public class LoggedUserControllerTest extends AbstractControllerTest{

	// 因为 config 中的 githubLoginService 没有创建 bean，所以这里 mock 一个
	@MockBean
	private GithubLoginService githubLoginService;
	
	@Test
	public void get_logged_user_not_logged() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user")
		.then()
			.statusCode(HttpStatus.SC_OK).body(containsString("{}"));
	}
	
	// 这个测试只会走 UsernamePasswordAuthenticationToken
	@WithMockUser("login_name")
	@Test
	public void get_logged_user_logged() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user")
		.then()
			.statusCode(HttpStatus.SC_OK).body(
					"loginName", equalTo("login_name"));
	}
}
