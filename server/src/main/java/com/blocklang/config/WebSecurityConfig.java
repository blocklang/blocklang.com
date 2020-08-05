package com.blocklang.config;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.blocklang.core.constant.WebSite;
import com.blocklang.core.filter.AutoLoginFilter;
import com.blocklang.core.filter.RouterFilter;
import com.blocklang.core.oauth2.CustomOAuth2AccessTokenResponseClient;
import com.blocklang.core.oauth2.CustomOAuth2UserService;
import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.QqLoginService;
import com.blocklang.core.service.UserBindService;
import com.blocklang.core.service.UserService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private GithubLoginService githubLoginService;
	@Autowired
	private QqLoginService qqLoginService;
	@Autowired
	private UserBindService userBindService;
	@Autowired
	private UserService userService;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private MessageSource messageSource;
	
	// 支持 oauth2 client
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		// We recommend disabling CSRF protection completely only if you are creating a
		// service that is used by non-browser clients
		// TODO: 考虑通过将服务拆分到单独项目中，然后打开此功能
		http.csrf().disable();
		http
			.addFilterBefore(new RouterFilter(), AnonymousAuthenticationFilter.class)
			.addFilterBefore(new AutoLoginFilter(userService), AnonymousAuthenticationFilter.class);
		
		//http.exceptionHandling().authenticationEntryPoint();
			
//		http.authorizeRequests().anyRequest().authenticated().and().oauth2Login().loginPage("/")
//		.and().oauth2Client();
		http//.authorizeRequests().antMatchers("/user").permitAll().anyRequest().authenticated().and()
			.logout()
				.logoutSuccessUrl(WebSite.HOME_URL) // 因为是 single page app，所以注销成功后返回首页
				.and()
			.oauth2Login()
				.loginPage(WebSite.HOME_URL) // 因为是 single page app，所以将登录页设置为首页
				.tokenEndpoint()
					.accessTokenResponseClient(new CustomOAuth2AccessTokenResponseClient())
				.and()
				.failureHandler((HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.setContentType("application/json;charset=utf-8");
					PrintWriter out = response.getWriter();
					out.write(exception.getMessage());
					out.flush();
					out.close();
				})
				.userInfoEndpoint()
					.userService(new CustomOAuth2UserService(
							githubLoginService, 
							qqLoginService, 
							userService, 
							userBindService, 
							propertyService,
							messageSource));

	}

}
