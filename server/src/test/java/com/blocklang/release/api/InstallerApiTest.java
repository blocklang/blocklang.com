package com.blocklang.release.api;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.blocklang.release.data.RegistrationInfo;

import static org.hamcrest.Matchers.*;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

@RunWith(SpringRunner.class)
@WebMvcTest(InstallerApi.class)
public class InstallerApiTest {
	
	@Autowired
	private MockMvc mvc;
	
	@Before
	public void setUp() {
		RestAssuredMockMvc.mockMvc(mvc);
	}
	
	@Test
	public void post_installer() throws Exception {
		RegistrationInfo registration = new RegistrationInfo();
		registration.setRegistrationToken("register_token");
		registration.setAppRunPort(8080);
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(201)
			.body(
				"installerToken", equalTo("register_token"), 
				"appRunPort", is(8080),
				"appName", is(notNullValue()),
				"appVersion", is(notNullValue()),
				"appFileName", is(notNullValue()),
				"jdkName", is(notNullValue()),
				"jdkVersion", is(notNullValue()),
				"jdkFileName", is(notNullValue()));
	}
}
