package com.blocklang.core.oauth2;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.oauth2.qq.QqOauth2UserService;
import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.service.QqLoginService;

public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>{

	private GithubLoginService githubLoginService;
	private QqLoginService qqLoginService;
	
	private DefaultOAuth2UserService defaultOAuth2UserService;
	private QqOauth2UserService qqUserService;
	
	public CustomOAuth2UserService(GithubLoginService githubLoginService, QqLoginService qqLoginService) {
		defaultOAuth2UserService = new DefaultOAuth2UserService();
		qqUserService = new QqOauth2UserService();
		
		this.githubLoginService = githubLoginService;
		this.qqLoginService = qqLoginService;
	}
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		
		OAuth2AccessToken accessToken = userRequest.getAccessToken();
		OAuth2User oauthUser = null;
		UserInfo userInfo = null;
		if(registrationId.equalsIgnoreCase(OauthSite.GITHUB.getValue())) {
			oauthUser = defaultOAuth2UserService.loadUser(userRequest);
			userInfo = githubLoginService.updateUser(accessToken, oauthUser);
			
		}else if(registrationId.equalsIgnoreCase(OauthSite.QQ.getValue())) {
			oauthUser = qqUserService.loadUser(userRequest);
			userInfo = qqLoginService.updateUser(accessToken, oauthUser);
		}
		
		if(userInfo != null) {
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
	}

}