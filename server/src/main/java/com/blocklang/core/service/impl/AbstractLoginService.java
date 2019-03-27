package com.blocklang.core.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.dao.UserBindDao;
import com.blocklang.core.data.AccountInfo;
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

	@Autowired
	protected UserService userService;
	
	@Autowired
	protected UserBindDao userBindDao;
	
	public AccountInfo getThirdPartyUser(OAuth2User oauthUser) {
		String openId = oauthUser.getName();
		Map<String, Object> userAttributes = oauthUser.getAttributes();
		
		UserInfo userInfo = prepareUser(userAttributes);
		List<UserAvatar> userAvatars = prepareUserAvatars(userAttributes);
		UserBind userBind = prepareUserBind(openId);
		
		return new AccountInfo(userInfo, userAvatars, userBind);
	}

	// 准备数据时，不要放创建时间，要在保存时添加
	public UserBind prepareUserBind(String openId) {
		UserBind userBind = new UserBind();
		userBind.setSite(getOauthSite());
		userBind.setOpenId(Objects.toString(openId, null));
		
		return userBind;
	}
	
	protected abstract UserInfo prepareUser(Map<String, Object> thirdPartyUser);
	protected abstract List<UserAvatar> prepareUserAvatars(Map<String, Object> thirdPartyUser);
	
	protected abstract String getSmallAvatarUrl(String avatarUrl);
	protected abstract String getMediumAvatarUrl(String avatarUrl);
	protected abstract String getLargeAvatarUrl(String avatarUrl);
	
	protected abstract OauthSite getOauthSite();
}
