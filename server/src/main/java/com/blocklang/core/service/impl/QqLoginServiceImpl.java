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
import com.blocklang.core.service.QqLoginService;
import com.blocklang.core.util.UrlUtil;

@Service
public class QqLoginServiceImpl extends AbstractLoginService implements QqLoginService {

	@Override
	public OauthSite getOauthSite() {
		return OauthSite.QQ;
	}

	// 准备数据时，不要包含创建时间和最近登录时间，要放在保存方法中
	@Override
	public UserInfo prepareUser(Map<String, Object> userAttributes) {
		UserInfo userInfo = new UserInfo();
		// qq 互联中没有 loginName 信息
		// userInfo.setLoginName(Objects.toString(userAttributes.get("login"), null));
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
		return userInfo;
	}
	
	// 准备数据时，不要放创建时间，要放在保存方法中
	@Override
	public List<UserAvatar> prepareUserAvatars(Map<String, Object> userAttributes) {
		List<UserAvatar> list = new ArrayList<UserAvatar>();
		
		UserAvatar smallAvatar = new UserAvatar();
		smallAvatar.setSizeType(AvatarSizeType.SMALL);
		String smallAvatarUrl = UrlUtil.trimHttpInUrl(Objects.toString(userAttributes.get("figureurl"), ""));
		smallAvatar.setAvatarUrl(smallAvatarUrl);
		list.add(smallAvatar);
		
		UserAvatar mediumAvatar = new UserAvatar();
		mediumAvatar.setSizeType(AvatarSizeType.MEDIUM);
		String mediumAvatarUrl = UrlUtil.trimHttpInUrl(Objects.toString(userAttributes.get("figureurl_1"), ""));
		mediumAvatar.setAvatarUrl(mediumAvatarUrl);
		list.add(mediumAvatar);
		
		UserAvatar largeAvatar = new UserAvatar();
		largeAvatar.setSizeType(AvatarSizeType.LARGE);
		String largeAvatarUrl = UrlUtil.trimHttpInUrl(Objects.toString(userAttributes.get("figureurl_2"), ""));
		largeAvatar.setAvatarUrl(largeAvatarUrl);
		list.add(largeAvatar);
		
		return list;
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
