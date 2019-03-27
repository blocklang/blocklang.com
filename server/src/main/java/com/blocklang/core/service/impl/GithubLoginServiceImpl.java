package com.blocklang.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.blocklang.core.constant.AvatarSizeType;
import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.GithubLoginService;

@Service
public class GithubLoginServiceImpl extends AbstractLoginService implements GithubLoginService {
	
	@Override
	public OauthSite getOauthSite() {
		return OauthSite.GITHUB;
	}

	// 准备数据时，不要包含创建时间和最近登录时间，要放在保存方法中
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

		return userInfo;
	}
	
	// 准备数据时，不要放创建时间，要放在保存方法中
	@Override
	public List<UserAvatar> prepareUserAvatars(Map<String, Object> userAttributes) {
		String avatarUrl = Objects.toString(userAttributes.get("avatar_url"), "");
		
		List<UserAvatar> list = new ArrayList<UserAvatar>();
		
		UserAvatar smallAvatar = new UserAvatar();
		smallAvatar.setSizeType(AvatarSizeType.SMALL);
		smallAvatar.setAvatarUrl(getSmallAvatarUrl(avatarUrl));
		list.add(smallAvatar);
		
		UserAvatar mediumAvatar = new UserAvatar();
		mediumAvatar.setSizeType(AvatarSizeType.MEDIUM);
		mediumAvatar.setAvatarUrl(getMediumAvatarUrl(avatarUrl));
		list.add(mediumAvatar);
		
		UserAvatar largeAvatar = new UserAvatar();
		largeAvatar.setSizeType(AvatarSizeType.LARGE);
		largeAvatar.setAvatarUrl(getLargeAvatarUrl(avatarUrl));
		list.add(largeAvatar);
		
		return list;
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

}
