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
import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.test.AbstractServiceTest;

public class GithubLoginServiceImplTest extends AbstractServiceTest{

	@Autowired
	private GithubLoginService githubLoginService;
	
	@Test
	public void get_third_party_user_success() {
		Map<String, Object> userAttributes = new HashMap<String, Object>();
		userAttributes.put("login", "login");
		userAttributes.put("id", "1");
		userAttributes.put("avatar_url", "https://avatars2.githubusercontent.com/u/1?v=4");
		userAttributes.put("name", "name");
		userAttributes.put("company", "company");
		userAttributes.put("blog", "blog");
		userAttributes.put("location", "location");
		userAttributes.put("email", "email");
		userAttributes.put("bio", "bio");
		
		List<OAuth2UserAuthority> authorities = new ArrayList<OAuth2UserAuthority>();
		OAuth2UserAuthority authority = new OAuth2UserAuthority(userAttributes);
		authorities.add(authority);

		OAuth2User oauthUser = new DefaultOAuth2User(authorities, userAttributes, "id");
		
		AccountInfo accountInfo = githubLoginService.getThirdPartyUser(oauthUser);
		
		UserInfo user = accountInfo.getUserInfo();
		assertThat(user.getLoginName()).isEqualTo("login");
		assertThat(user.getNickname()).isEqualTo("name");
		assertThat(user.getAvatarUrl()).isEqualTo("https://avatars2.githubusercontent.com/u/1?v=4&s=40");
		assertThat(user.getCompany()).isEqualTo("company");
		assertThat(user.getWebsiteUrl()).isEqualTo("blog");
		assertThat(user.getLocation()).isEqualTo("location");
		assertThat(user.getEmail()).isEqualTo("email");
		assertThat(user.getBio()).isEqualTo("bio");
		
		UserBind userBind = accountInfo.getUserBind();
		assertThat(userBind.getOpenId()).isEqualTo("1");
		
		List<UserAvatar> avatars =accountInfo.getAvatarList();
		assertThat(avatars.stream().map(avatar -> avatar.getAvatarUrl()).collect(Collectors.toList()))
			.allMatch(s -> s.startsWith("https://avatars2.githubusercontent.com/u/1?v=4&s="));
	}

}
