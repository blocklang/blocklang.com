package com.blocklang.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.blocklang.core.service.GithubLoginService;

@EnableWebSecurity
public class OAuth2LoginSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private GithubLoginService githubLoginService;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// We recommend disabling CSRF protection completely only if you are creating a
		// service that is used by non-browser clients
		// TODO: 考虑通过将服务拆分到单独项目中，然后打开此功能
		http.csrf().disable();
		
//		http.authorizeRequests().anyRequest().authenticated().and().oauth2Login().loginPage("/")
//		.and().oauth2Client();
		http//.authorizeRequests().antMatchers("/user").permitAll().anyRequest().authenticated().and()
			.oauth2Login()
				.loginPage("/").userInfoEndpoint().userService(this.oauth2UserService()); // 因为是 single page app，所以将登录页设置为首页
	}
	
	private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
		final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
		return (userRequest) -> {
			String registrationId = userRequest.getClientRegistration().getRegistrationId();
			
			// Delegate to the default implementation for loading a user
			OAuth2User oauthUser = delegate.loadUser(userRequest);
			OAuth2AccessToken accessToken = userRequest.getAccessToken();
			
			if(registrationId.equals("github")) {
				githubLoginService.updateUser(accessToken, oauthUser);
			}

			return oauthUser;
		};
	}

}
