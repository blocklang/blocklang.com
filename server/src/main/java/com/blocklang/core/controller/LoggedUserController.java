package com.blocklang.core.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 获取登录用户常用信息
 * 
 * @author Zhengwei Jin
 *
 */
@RestController
public class LoggedUserController {

	@GetMapping("/user")
	public ResponseEntity<Map<String, Object>> index(Principal principal) {
		Map<String, Object> user = new HashMap<String, Object>();
		if (principal != null) {
			if(OAuth2AuthenticationToken.class.isInstance(principal)) {
				OAuth2AuthenticationToken token = (OAuth2AuthenticationToken)principal;
				Map<String, Object> userAttributes = token.getPrincipal().getAttributes();
				// 因为客户端并不需要显示登录用户的登录标识，所以不返回 userId
				user.put("loginName", userAttributes.get("loginName"));
				user.put("avatarUrl", userAttributes.get("avatarUrl"));
			} else if(UsernamePasswordAuthenticationToken.class.isInstance(principal)) {
				UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)principal;
				User securityUser = (User)token.getPrincipal();
				// 因为客户端并不需要显示登录用户的登录标识，所以不返回 userId
				user.put("loginName", securityUser.getUsername());
			}
		} else {
			// 用户未登录
		}

		return new ResponseEntity<Map<String, Object>>(user, HttpStatus.OK);
	}
	
}
