package com.blocklang.core.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.model.UserInfo;

public interface LoginService {

	/**
	 * 用第三方网站的用户信息更新用户信息
	 * 
	 * @param accessToken
	 * @param oauthUser
	 * @return 返回用户标识
	 */
	int updateUser(OAuth2AccessToken accessToken, OAuth2User oauthUser);
	
	UserInfo prepareUser(Map<String, Object> thirdPartyUser);
	List<UserAvatar> prepareUserAvatars(Map<String, Object> thirdPartyUser);
	UserBind prepareUserBind(String openId);
	
	String getSmallAvatarUrl(String avatarUrl);
	String getMediumAvatarUrl(String avatarUrl);
	String getLargeAvatarUrl(String avatarUrl);

}
