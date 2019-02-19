package com.blocklang.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.constant.WebSite;
import com.blocklang.core.filter.RouterFilter;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.GithubLoginService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private GithubLoginService githubLoginService;
	
	// 支持 oauth2 client
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// We recommend disabling CSRF protection completely only if you are creating a
		// service that is used by non-browser clients
		// TODO: 考虑通过将服务拆分到单独项目中，然后打开此功能
		http.csrf().disable();
		http.addFilterBefore(new RouterFilter(), AnonymousAuthenticationFilter.class);
		
//		http.authorizeRequests().anyRequest().authenticated().and().oauth2Login().loginPage("/")
//		.and().oauth2Client();
		http//.authorizeRequests().antMatchers("/user").permitAll().anyRequest().authenticated().and()
			.logout()
				.logoutSuccessUrl(WebSite.HOME_URL) // 因为是 single page app，所以注销成功后返回首页
				.and()
			.oauth2Login()
				.loginPage(WebSite.HOME_URL) // 因为是 single page app，所以将登录页设置为首页
				.userInfoEndpoint()
					.userService(this.oauth2UserService());
	}
	
	private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
		final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
		return (userRequest) -> {
			String registrationId = userRequest.getClientRegistration().getRegistrationId();
			
			// Delegate to the default implementation for loading a user
			OAuth2User oauthUser = delegate.loadUser(userRequest);
			OAuth2AccessToken accessToken = userRequest.getAccessToken();
			
			if(registrationId.equalsIgnoreCase(OauthSite.GITHUB.getValue())) {
				UserInfo userInfo = githubLoginService.updateUser(accessToken, oauthUser);
				// 将第三方用户信息转换为本网站的用户信息
				// 这里主要是存储用户 id 等页面上常用信息
				Map<String, Object> userAttributes = new HashMap<String, Object>();
				userAttributes.put("id", userInfo.getId());
				userAttributes.put("loginName", userInfo.getLoginName());
				userAttributes.put("avatarUrl", userInfo.getAvatarUrl());
				// 经过反复考虑，这里设置 loginName 而不是设置 id
				// 首先将 loginName 加上唯一约束后，使用 byLoginName 和 byId 获取用户信息的效果是一样的
				// 这样在写代码时，不要做各种转换
				return new DefaultOAuth2User(oauthUser.getAuthorities(), userAttributes, "loginName");
			}

			return oauthUser;
		};
	}

}
