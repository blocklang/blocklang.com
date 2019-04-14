package com.blocklang.core.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
	
}
