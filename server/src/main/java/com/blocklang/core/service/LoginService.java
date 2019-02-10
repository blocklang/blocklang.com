package com.blocklang.core.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.dao.UserBindDao;
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
	default int updateUser(OAuth2AccessToken accessToken, OAuth2User oauthUser) {
		String openId = oauthUser.getName();
		Map<String, Object> userAttributes = oauthUser.getAttributes();
		
		UserInfo userInfo = prepareUser(userAttributes);
		List<UserAvatar> userAvatars = prepareUserAvatars(userAttributes);
		UserBind userBind = prepareUserBind(openId);
		
		Optional<UserBind> userBindOption = getUserBindDao().findBySiteAndOpenId(getOauthSite(), openId);
		if(userBindOption.isEmpty()) {
			Integer userId = getUserService().create(userInfo, userBind, userAvatars);
			return userId;
		} else {
			Integer savedUserId = userBindOption.get().getUserId();
			getUserService().update(savedUserId, userInfo, userAvatars);
			return savedUserId;
		}
	}
	
	UserInfo prepareUser(Map<String, Object> thirdPartyUser);
	List<UserAvatar> prepareUserAvatars(Map<String, Object> thirdPartyUser);
	UserBind prepareUserBind(String openId);
	
	String getSmallAvatarUrl(String avatarUrl);
	String getMediumAvatarUrl(String avatarUrl);
	String getLargeAvatarUrl(String avatarUrl);
	
	OauthSite getOauthSite();
	
	UserService getUserService();
	
	UserBindDao getUserBindDao();

}
