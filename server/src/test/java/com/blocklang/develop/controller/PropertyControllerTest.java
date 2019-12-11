package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import com.blocklang.core.test.AbstractControllerTest;

import io.restassured.http.ContentType;

@WebMvcTest(PropertyController.class)
public class PropertyControllerTest extends AbstractControllerTest{

	@Test
	public void get_properties_not_exist() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/properties/{name}", "not-exist")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(0));
	}
	
	@Test
	public void get_app_types() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/properties/{name}", "app-type")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(1)); // 0.1.1 版本只支持 web
	}
	
}
