package com.blocklang.core.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.data.UpdateUserParam;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;

import io.restassured.http.ContentType;

@WebMvcTest(LoggedUserController.class)
public class LoggedUserControllerTest extends AbstractControllerTest{
	
	@Test
	public void get_logged_user_not_logged() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user")
		.then()
			.statusCode(HttpStatus.SC_OK).body("status", equalTo("NotLogin"));
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
			.statusCode(HttpStatus.SC_OK)
			.body("loginName", equalTo("login_name"));
	}
	
	@Test
	public void get_profile_user_not_logged() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user/profile")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("login_name")
	@Test
	public void get_profile_success() {
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user/profile")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("id", equalTo(1));
	}
	
	@Test
	public void update_profile_user_not_login() {
		UpdateUserParam param = new UpdateUserParam();
		param.setId(1);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.put("/user/profile")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("login_name")
	@Test
	public void update_profile_success() {
		UpdateUserParam param = new UpdateUserParam();
		param.setId(1);
		param.setNickname("new-nickname");
		
		UserInfo existUser = new UserInfo();
		existUser.setId(1);
		existUser.setNickname("old-nickname");
		when(userService.findById(param.getId())).thenReturn(Optional.of(existUser));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.put("/user/profile")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("nickname", equalTo("new-nickname"),
					"lastUpdateTime", is(notNullValue()));
		
		verify(userService).update(any());
	}
}
