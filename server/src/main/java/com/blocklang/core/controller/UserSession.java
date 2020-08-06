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
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.blocklang.core.model.UserInfo;

public abstract class UserSession {

	private static final String THIRD_PARTY_SESSION_KEY = "x-third-party-user";
	
	/**
	 * 使用 github 等帐号登录出错时，需要将错误信息缓存到 session 中，此时使用此值作为 session 的 key
	 */
	private static final String LOGIN_ERROR_MESSAGE = "login.error.message";
	
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
	
	/**
	 * 用户登录失败时，将错误信息缓存在 Session 中
	 * @param errorMessage 错误信息
	 */
	public static void loginFailure(String errorMessage) {
		Assert.notNull(errorMessage, "必须要传入错误信息");
		getSession().setAttribute(LOGIN_ERROR_MESSAGE, errorMessage);
	}
	
	public static boolean loginFailure() {
		return getSession().getAttribute(LOGIN_ERROR_MESSAGE) != null;
	}
	
	public static String removeLoginFailureMessage() {
		String loginFailureMessage = (String) getSession().getAttribute(LOGIN_ERROR_MESSAGE);
		getSession().removeAttribute(LOGIN_ERROR_MESSAGE);
		return loginFailureMessage;
	}
	
}
