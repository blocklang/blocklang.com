package com.blocklang.core.service.impl;

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
import com.blocklang.core.service.UserService;

/**
 * 之前考虑将登录相关的共用方法都提取成接口，但是这样就暴露出很多外部并不需要的内部接口，所以决定将内部接口移到抽象类中。
 * 
 * @author Zhengwei Jin
 *
 */
public abstract class AbstractLoginService {

	public UserInfo updateUser(OAuth2AccessToken accessToken, OAuth2User oauthUser) {
		String openId = oauthUser.getName();
		Map<String, Object> userAttributes = oauthUser.getAttributes();
		
		UserInfo userInfo = prepareUser(userAttributes);
		List<UserAvatar> userAvatars = prepareUserAvatars(userAttributes);
		UserBind userBind = prepareUserBind(openId);
		
		Optional<UserBind> userBindOption = getUserBindDao().findBySiteAndOpenId(getOauthSite(), openId);
		if(userBindOption.isEmpty()) {
			return getUserService().create(userInfo, userBind, userAvatars);
		} else {
			Integer savedUserId = userBindOption.get().getUserId();
			return getUserService().update(savedUserId, userInfo, userAvatars);
		}
	}
	
	protected abstract UserInfo prepareUser(Map<String, Object> thirdPartyUser);
	protected abstract List<UserAvatar> prepareUserAvatars(Map<String, Object> thirdPartyUser);
	protected abstract UserBind prepareUserBind(String openId);
	
	protected abstract String getSmallAvatarUrl(String avatarUrl);
	protected abstract String getMediumAvatarUrl(String avatarUrl);
	protected abstract String getLargeAvatarUrl(String avatarUrl);
	
	protected abstract OauthSite getOauthSite();
	
	protected abstract UserService getUserService();
	
	protected abstract UserBindDao getUserBindDao();
	
}
