package com.blocklang.core.util;

import java.security.Principal;

import org.springframework.security.core.context.SecurityContextHolder;

public abstract class SecurityUtil {

	public static Principal getLoginUser() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
}
