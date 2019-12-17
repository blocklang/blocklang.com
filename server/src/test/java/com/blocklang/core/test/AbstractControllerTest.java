package com.blocklang.core.test;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.QqLoginService;
import com.blocklang.core.service.UserBindService;
import com.blocklang.core.service.UserService;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

public class AbstractControllerTest extends AbstractSpringTest{

	// 添加了 oauth2 后提示没有找到 bean，所以这里 mock 一个
	// TODO: 学习此测试用例，以找出原因
	// https://github.com/spring-projects/spring-security/blob/master/samples/boot/oauth2login/src/integration-test/java/org/springframework/security/samples/OAuth2LoginApplicationTests.java
	@MockBean
	private ClientRegistrationRepository clientRegistrationRepository;
	
	// 以下 bean 在 WebSecurityConfig 类中要使用，但是在运行测试用例时找不到 bean，所以这里统一 mock
	@MockBean
	private GithubLoginService githubLoginService;
	@MockBean
	private QqLoginService qqLoginService;
	@MockBean
	protected UserBindService userBindService;
	@MockBean
	protected UserService userService;
	@MockBean
	protected PropertyService propertyService;
	
	@Autowired
	private MockMvc mvc;
	
	@BeforeEach
	public void setUp() {
		RestAssuredMockMvc.mockMvc(mvc);
	}
}
