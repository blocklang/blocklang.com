package com.blocklang.core.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.service.DocumentService;
import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.test.AbstractControllerTest;

import io.restassured.http.ContentType;

@WebMvcTest(DocumentController.class)
public class DocumentControllerTest extends AbstractControllerTest{

	// 因为 config 中的 githubLoginService 没有创建 bean，所以这里 mock 一个
	@MockBean
	private GithubLoginService githubLoginService;
	@MockBean
	private DocumentService documentService;
	
	@Test
	public void get_document_file_not_found() {
		when(documentService.findByFileName(anyString())).thenReturn(Optional.empty());
		given()
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
			.contentType(ContentType.TEXT)
		.when()
			.get("/docs/{fileName}", "help")
		.then()
			.statusCode(HttpStatus.SC_OK).body(equalTo("content"));
	}
}
