package com.blocklang.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import com.blocklang.core.data.AccountInfo;
import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.QqLoginService;
import com.blocklang.core.test.AbstractServiceTest;

public class QqLoginServiceImplTest extends AbstractServiceTest {
	
	@Autowired
	private QqLoginService qqLoginService;
	
	@Test
	public void get_third_party_user_success() {
		Map<String, Object> userAttributes = new HashMap<String, Object>();
		userAttributes.put("nickname", "name");
		
		userAttributes.put("openid", "1");
		userAttributes.put("figureurl", "https://figureurl");
		userAttributes.put("figureurl_1", "https://figureurl_1");
		userAttributes.put("figureurl_2", "https://figureurl_2");
		userAttributes.put("gender", "男");
		
		List<OAuth2UserAuthority> authorities = new ArrayList<OAuth2UserAuthority>();
		OAuth2UserAuthority authority = new OAuth2UserAuthority(userAttributes);
		authorities.add(authority);

		OAuth2User oauthUser = new DefaultOAuth2User(authorities, userAttributes, "openid");

		AccountInfo accountInfo = qqLoginService.getThirdPartyUser(oauthUser);
		
		UserInfo user = accountInfo.getUserInfo();
		assertThat(user.getLoginName()).isNullOrEmpty();
		assertThat(user.getNickname()).isEqualTo("name");
		assertThat(user.getAvatarUrl()).isEqualTo("https://figureurl");
		assertThat(user.getCompany()).isNullOrEmpty();
		assertThat(user.getWebsiteUrl()).isNullOrEmpty();
		assertThat(user.getLocation()).isNullOrEmpty();
		assertThat(user.getEmail()).isNullOrEmpty();
		assertThat(user.getBio()).isNullOrEmpty();
		
		UserBind userBind = accountInfo.getUserBind();
		assertThat(userBind.getOpenId()).isEqualTo("1");
		
		List<UserAvatar> avatars =accountInfo.getAvatarList();
		assertThat(avatars.stream().map(avatar -> avatar.getAvatarUrl()).collect(Collectors.toList()))
			.allMatch(s -> s.startsWith("https://figureurl"));
	}
}
