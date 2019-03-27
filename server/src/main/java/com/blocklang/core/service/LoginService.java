package com.blocklang.core.service;

import org.springframework.security.oauth2.core.user.OAuth2User;

import com.blocklang.core.data.AccountInfo;

public interface LoginService {

	AccountInfo getThirdPartyUser(OAuth2User oauthUser);
}
