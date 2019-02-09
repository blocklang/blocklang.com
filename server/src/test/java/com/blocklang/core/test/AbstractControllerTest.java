package com.blocklang.core.test;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
public class AbstractControllerTest {

	// 添加了 oauth2 后提示没有找到 bean，所以这里 mock 一个
	// TODO: 学习此测试用例，以找出原因
	// https://github.com/spring-projects/spring-security/blob/master/samples/boot/oauth2login/src/integration-test/java/org/springframework/security/samples/OAuth2LoginApplicationTests.java
	@MockBean
	private ClientRegistrationRepository clientRegistrationRepository;
	
	@Autowired
	private MockMvc mvc;
	
	@Before
	public void setUp() {
		RestAssuredMockMvc.mockMvc(mvc);
	}
}
