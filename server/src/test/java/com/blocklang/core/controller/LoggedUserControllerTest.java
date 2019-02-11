package com.blocklang.core.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

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
	
	// TODO: 如何测试 Principal 有值的情况？
	@Ignore
	@Test
	public void get_logged_user_logged() {
		Map<String, Object> userAttributes = new HashMap<String, Object>();
		userAttributes.put("id", "id");
		userAttributes.put("loginName", "login_name");
		userAttributes.put("avatarUrl", "avatar_url");
		
		List<OAuth2UserAuthority> authorities = new ArrayList<OAuth2UserAuthority>();
		OAuth2UserAuthority authority = new OAuth2UserAuthority(userAttributes);
		authorities.add(authority);
		
		OAuth2User principal =new DefaultOAuth2User(authorities, userAttributes, "id");
		
		OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(principal, authorities, "github");
		given().auth().principal(token)
			.contentType(ContentType.JSON)
		.when()
			.get("/user")
		.then()
			.statusCode(HttpStatus.SC_OK).body(
					"loginName", equalTo("login_name"),
					"avatarUrl", equalTo("avatar_url"));
	}
}
