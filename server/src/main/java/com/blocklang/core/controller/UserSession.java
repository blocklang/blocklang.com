package com.blocklang.core.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.blocklang.core.model.UserInfo;

public abstract class UserSession {

	private static final String THIRD_PARTY_SESSION_KEY = "x-third-party-user";
	
	public static void setThirdPartyUser(Map<String, Object> thirdPartyUser) {
		HttpSession httpSession = getSession();
		httpSession.setAttribute(THIRD_PARTY_SESSION_KEY, thirdPartyUser);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getThirdPartyUser(){
		HttpSession session = getSession();
		return (Map<String, Object>) session.getAttribute(THIRD_PARTY_SESSION_KEY);
	}

	public static void removeThirdPartyUser(){
		HttpSession httpSession = getSession();
		httpSession.removeAttribute(THIRD_PARTY_SESSION_KEY);
	}

	private static HttpSession getSession() {
		return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getSession();
	}
	
	public static void storeUserToSecurityContext(String registrationId, UserInfo userInfo, String loginToken) {
		Map<String, Object> userAttributes = new HashMap<String, Object>();
		userAttributes.put("id", userInfo.getId());
		userAttributes.put("loginName", userInfo.getLoginName());
		userAttributes.put("avatarUrl", userInfo.getAvatarUrl());
		userAttributes.put("token", loginToken);
		
		Set<GrantedAuthority> authorities = Collections.singleton(new OAuth2UserAuthority(userAttributes));
		OAuth2User oauth2User = new DefaultOAuth2User(authorities, userAttributes, "loginName");
		OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(oauth2User, authorities, registrationId);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
	
}
