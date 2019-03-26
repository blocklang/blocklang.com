package com.blocklang.core.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.blocklang.core.constant.AvatarSizeType;
import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.QqLoginService;
import com.blocklang.core.util.UrlUtil;

@Service
public class QqLoginServiceImpl extends AbstractLoginService implements QqLoginService {

	@Override
	public OauthSite getOauthSite() {
		return OauthSite.QQ;
	}

	@Override
	public List<UserAvatar> prepareUserAvatars(Map<String, Object> userAttributes) {
		List<UserAvatar> list = new ArrayList<UserAvatar>();
		
		UserAvatar smallAvatar = new UserAvatar();
		smallAvatar.setSizeType(AvatarSizeType.SMALL);
		smallAvatar.setCreateTime(LocalDateTime.now());
		String smallAvatarUrl = UrlUtil.trimHttpInUrl(Objects.toString(userAttributes.get("figureurl"), ""));
		smallAvatar.setAvatarUrl(smallAvatarUrl);
		list.add(smallAvatar);
		
		UserAvatar mediumAvatar = new UserAvatar();
		mediumAvatar.setSizeType(AvatarSizeType.MEDIUM);
		mediumAvatar.setCreateTime(LocalDateTime.now());
		String mediumAvatarUrl = UrlUtil.trimHttpInUrl(Objects.toString(userAttributes.get("figureurl_1"), ""));
		mediumAvatar.setAvatarUrl(mediumAvatarUrl);
		list.add(mediumAvatar);
		
		UserAvatar largeAvatar = new UserAvatar();
		largeAvatar.setSizeType(AvatarSizeType.LARGE);
		largeAvatar.setCreateTime(LocalDateTime.now());
		String largeAvatarUrl = UrlUtil.trimHttpInUrl(Objects.toString(userAttributes.get("figureurl_2"), ""));
		largeAvatar.setAvatarUrl(largeAvatarUrl);
		list.add(largeAvatar);
		
		return list;
	}

	@Override
	public UserInfo prepareUser(Map<String, Object> userAttributes) {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName(Objects.toString(userAttributes.get("login"), null));
		userInfo.setNickname(Objects.toString(userAttributes.get("nickname"), null));
		userInfo.setEnabled(true);
		userInfo.setAdmin(false);
		String smallAvatarUrl = UrlUtil.trimHttpInUrl(Objects.toString(userAttributes.get("figureurl"), ""));
		userInfo.setAvatarUrl(smallAvatarUrl);
		userInfo.setEmail(Objects.toString(userAttributes.get("email"), null));
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
	protected String getSmallAvatarUrl(String avatarUrl) {
		return null;
	}

	@Override
	protected String getMediumAvatarUrl(String avatarUrl) {
		return null;
	}

	@Override
	protected String getLargeAvatarUrl(String avatarUrl) {
		return null;
	}

}
