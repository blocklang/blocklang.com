package com.blocklang.core.service;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface GithubLoginService {

	/**
	 * 用第三方网站的用户信息更新用户信息
	 * 
	 * @param accessToken
	 * @param oauthUser
	 * @return 返回用户标识
	 */
	int updateUser(OAuth2AccessToken accessToken, OAuth2User oauthUser);

}
