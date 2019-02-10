package com.blocklang.core.service;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.blocklang.core.model.UserInfo;

public interface LoginService {

	/**
	 * 用第三方网站的用户信息更新用户信息
	 * 
	 * @param accessToken
	 * @param oauthUser
	 * @return 用户信息
	 */
	UserInfo updateUser(OAuth2AccessToken accessToken, OAuth2User oauthUser);

}
