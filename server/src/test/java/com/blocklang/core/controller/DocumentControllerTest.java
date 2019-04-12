package com.blocklang.core.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.service.DocumentService;
import com.blocklang.core.test.AbstractControllerTest;

import io.restassured.http.ContentType;

@WebMvcTest(DocumentController.class)
public class DocumentControllerTest extends AbstractControllerTest{

	@MockBean
	private DocumentService documentService;
	
	@Test
	public void get_document_file_not_found() {
		when(documentService.findByFileName(anyString())).thenReturn(Optional.empty());
		given()
			.header(HttpCustomHeader.KEY_REQUEST_WITH, HttpCustomHeader.VALUE_FETCH_API) // 注意，因为路由的值与 rest api 的值相同，需要 X-Request-With 区分。
			.contentType(ContentType.TEXT)
		.when()
			.get("/docs/{fileName}", "help")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_document_success() {
		when(documentService.findByFileName(anyString())).thenReturn(Optional.of("content"));
		given()
			.header(HttpCustomHeader.KEY_REQUEST_WITH, HttpCustomHeader.VALUE_FETCH_API) // 注意，因为路由的值与 rest api 的值相同，需要 X-Request-With 区分。
			.contentType(ContentType.TEXT)
		.when()
			.get("/docs/{fileName}", "help")
		.then()
			.statusCode(HttpStatus.SC_OK).body(equalTo("content"));
	}
}
