package com.blocklang.core.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.AvatarSizeType;
import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.dao.UserAvatarDao;
import com.blocklang.core.dao.UserBindDao;
import com.blocklang.core.dao.UserDao;
import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.GithubLoginService;

@Service
public class GithubLoginServiceImpl implements GithubLoginService {
	
	@Autowired
	private UserDao userDao;
	@Autowired
	private UserAvatarDao userAvatarDao;
	@Autowired
	private UserBindDao userBindDao;
	
	@Override
	public int updateUser(OAuth2AccessToken accessToken, OAuth2User oauthUser) {
		
		Map<String, Object> userAttributes = oauthUser.getAttributes();
		
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
		
		UserInfo savedUserInfo = userDao.save(userInfo);
		
		Integer userId = savedUserInfo.getId();
		
		String avatarUrl = Objects.toString(userAttributes.get("avatar_url"), "");
		List<UserAvatar> list = new ArrayList<UserAvatar>();
		UserAvatar smallAvatar = new UserAvatar();
		smallAvatar.setUserId(userId);
		smallAvatar.setSizeType(AvatarSizeType.SMALL);
		smallAvatar.setCreateTime(LocalDateTime.now());
		smallAvatar.setAvatarUrl(getSmallAvatarUrl(avatarUrl));
		list.add(smallAvatar);
		UserAvatar mediumAvatar = new UserAvatar();
		mediumAvatar.setUserId(userId);
		mediumAvatar.setSizeType(AvatarSizeType.MEDIUM);
		mediumAvatar.setCreateTime(LocalDateTime.now());
		mediumAvatar.setAvatarUrl(getMediumAvatarUrl(avatarUrl));
		list.add(mediumAvatar);
		UserAvatar largeAvatar = new UserAvatar();
		largeAvatar.setUserId(userId);
		largeAvatar.setSizeType(AvatarSizeType.LARGE);
		largeAvatar.setCreateTime(LocalDateTime.now());
		largeAvatar.setAvatarUrl(getLargeAvatarUrl(avatarUrl));
		list.add(largeAvatar);
		
		userAvatarDao.saveAll(list);
		
		UserBind userBind = new UserBind();
		userBind.setUserId(userId);
		userBind.setSite(OauthSite.GITHUB);
		userBind.setOpenId(Objects.toString(oauthUser.getName(), null));
		userBind.setCreateTime(LocalDateTime.now());
		userBindDao.save(userBind);
		
		return userId;
	}
	
	private String getSmallAvatarUrl(String avatarUrl) {
		if(avatarUrl == null) {
			return null;
		}
		
		return avatarUrl + "&s=40";
	}

	private String getMediumAvatarUrl(String avatarUrl) {
		if(avatarUrl == null) {
			return null;
		}
		
		return avatarUrl + "&s=200";
	}
	
	private String getLargeAvatarUrl(String avatarUrl) {
		if(avatarUrl == null) {
			return null;
		}
		
		return avatarUrl + "&s=460";
	}

}
