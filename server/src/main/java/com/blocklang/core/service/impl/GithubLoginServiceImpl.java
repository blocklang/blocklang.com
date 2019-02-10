package com.blocklang.core.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.AvatarSizeType;
import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.dao.UserBindDao;
import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.service.UserService;

@Service
public class GithubLoginServiceImpl implements GithubLoginService {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserBindDao userBindDao;

	@Override
	public OauthSite getOauthSite() {
		return OauthSite.GITHUB;
	}

	@Override
	public UserBind prepareUserBind(String openId) {
		UserBind userBind = new UserBind();
		userBind.setSite(getOauthSite());
		userBind.setOpenId(Objects.toString(openId, null));
		userBind.setCreateTime(LocalDateTime.now());
		return userBind;
	}

	@Override
	public List<UserAvatar> prepareUserAvatars(Map<String, Object> userAttributes) {
		String avatarUrl = Objects.toString(userAttributes.get("avatar_url"), "");
		
		List<UserAvatar> list = new ArrayList<UserAvatar>();
		
		UserAvatar smallAvatar = new UserAvatar();
		smallAvatar.setSizeType(AvatarSizeType.SMALL);
		smallAvatar.setCreateTime(LocalDateTime.now());
		smallAvatar.setAvatarUrl(getSmallAvatarUrl(avatarUrl));
		list.add(smallAvatar);
		
		UserAvatar mediumAvatar = new UserAvatar();
		mediumAvatar.setSizeType(AvatarSizeType.MEDIUM);
		mediumAvatar.setCreateTime(LocalDateTime.now());
		mediumAvatar.setAvatarUrl(getMediumAvatarUrl(avatarUrl));
		list.add(mediumAvatar);
		
		UserAvatar largeAvatar = new UserAvatar();
		largeAvatar.setSizeType(AvatarSizeType.LARGE);
		largeAvatar.setCreateTime(LocalDateTime.now());
		largeAvatar.setAvatarUrl(getLargeAvatarUrl(avatarUrl));
		list.add(largeAvatar);
		
		return list;
	}

	@Override
	public UserInfo prepareUser(Map<String, Object> userAttributes) {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName(Objects.toString(userAttributes.get("login"), null));
		userInfo.setNickname(Objects.toString(userAttributes.get("name"), null));
		userInfo.setEnabled(true);
		userInfo.setAdmin(false);
		userInfo.setAvatarUrl(getSmallAvatarUrl(Objects.toString(userAttributes.get("avatar_url"), null)));
		userInfo.setEmail(Objects.toString(userAttributes.get("email")));
		userInfo.setLocation(Objects.toString(userAttributes.get("location"), null));
		userInfo.setEmail(Objects.toString(userAttributes.get("email"), null));
		userInfo.setWebsiteUrl(Objects.toString(userAttributes.get("blog"), null));
		userInfo.setCompany(Objects.toString(userAttributes.get("company"), null));
		userInfo.setBio(Objects.toString(userAttributes.get("bio"), null));
		userInfo.setCreateTime(LocalDateTime.now());
		userInfo.setLastSignInTime(LocalDateTime.now()); // 设置最近登录时间。
		return userInfo;
	}
	
	@Override
	public String getSmallAvatarUrl(String avatarUrl) {
		if(avatarUrl == null) {
			return null;
		}
		
		return avatarUrl + "&s=40";
	}

	@Override
	public String getMediumAvatarUrl(String avatarUrl) {
		if(avatarUrl == null) {
			return null;
		}
		
		return avatarUrl + "&s=200";
	}
	
	@Override
	public String getLargeAvatarUrl(String avatarUrl) {
		if(avatarUrl == null) {
			return null;
		}
		
		return avatarUrl + "&s=460";
	}

	@Override
	public UserService getUserService() {
		return this.userService;
	}

	@Override
	public UserBindDao getUserBindDao() {
		return this.userBindDao;
	}

}
