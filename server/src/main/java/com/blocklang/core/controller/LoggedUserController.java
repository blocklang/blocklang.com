package com.blocklang.core.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.constant.LoginStatus;
import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.data.AccountInfo;
import com.blocklang.core.data.CheckLoginNameParam;
import com.blocklang.core.data.NewUserParam;
import com.blocklang.core.data.UpdateUserParam;
import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.UserService;

/**
 * 获取登录用户常用信息
 * 
 * @author Zhengwei Jin
 *
 */
@RestController
public class LoggedUserController {
	
	private static final Logger logger = LoggerFactory.getLogger(LoggedUserController.class);
	private static final String KEY_LOGIN_STATUS = "status";
	
	@Autowired
	private UserService userService;
	@Autowired
	private PropertyService propertyService;

	/**
	 * 本方法中包含以下逻辑：
	 * 
	 * <ul>
	 * <li> 1. 如果用户登录成功
	 * <li> 2. 如果用户登录成功，但还需要用户补充必填信息
	 * <li> 3. 用户登录失败
	 * <li> 4. 用户没有登录
	 * </ul>
	 * 
	 * 通过 status 来标识登录状态。
	 * 
	 * @param principal 登录用户的信息
	 * @return 登录用户相关信息
	 */
	@GetMapping("/user")
	public ResponseEntity<Map<String, Object>> index(Principal principal) {
		Map<String, Object> user = new HashMap<String, Object>();
		if (loginSuccess(principal)) {
			user.put(KEY_LOGIN_STATUS, LoginStatus.LOGINED.getStatus());
			if(OAuth2AuthenticationToken.class.isInstance(principal)) {
				OAuth2AuthenticationToken token = (OAuth2AuthenticationToken)principal;
				Map<String, Object> userAttributes = token.getPrincipal().getAttributes();
				// 因为客户端并不需要显示登录用户的登录标识，所以不返回 userId
				user.put("loginName", userAttributes.get("loginName"));
				user.put("avatarUrl", userAttributes.get("avatarUrl"));
				user.put("token", userAttributes.get("token"));
			} else if(UsernamePasswordAuthenticationToken.class.isInstance(principal)) {
				UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)principal;
				User securityUser = (User)token.getPrincipal();
				// 因为客户端并不需要显示登录用户的登录标识，所以不返回 userId
				user.put("loginName", securityUser.getUsername());
			}
		} else if(loginSuccessButNeedCompleteUserInfo()) {
			user.put(KEY_LOGIN_STATUS, LoginStatus.NEED_COMPLETE_USER_INFO.getStatus());
			// 判断 session 中是否存储第三方用户信息，如果存在的话，则需要完善用户信息
			Map<String, Object> thirdPartyUser = UserSession.getThirdPartyUser();
			AccountInfo accountInfo = (AccountInfo) thirdPartyUser.get("accountInfo");
			
			UserInfo userInfo = accountInfo.getUserInfo();
			user.put("loginName", userInfo.getLoginName());
			user.put("nickname", userInfo.getNickname());
			user.put("avatarUrl", userInfo.getAvatarUrl());
			user.put("loginNameErrorMessage", thirdPartyUser.get("loginNameErrorMessage"));				
		} else if(loginFailure()) {
			user.put(KEY_LOGIN_STATUS, LoginStatus.FAILED.getStatus());
			user.put("loginFailureMessage", UserSession.removeLoginFailureMessage());
		} else {
			// 用户未登录
			user.put(KEY_LOGIN_STATUS, LoginStatus.NOT_LOGIN.getStatus());
		}
		
		return new ResponseEntity<Map<String, Object>>(user, HttpStatus.OK);
	}

	private boolean loginSuccess(Principal principal) {
		return principal != null;
	}
	
	private boolean loginSuccessButNeedCompleteUserInfo() {
		return UserSession.getThirdPartyUser() != null;
	}
	
	private boolean loginFailure() {
		return UserSession.loginFailure();
	}
	
	@PostMapping("/user/check-login-name")
	public ResponseEntity<Map<String, Object>> checkLoginName(
			@Valid @RequestBody CheckLoginNameParam param, 
			BindingResult bindingResult) {
		
		UserValidator validator = new UserValidator(userService, propertyService, true);
		
		NewUserParam newUserParam = new NewUserParam();
		newUserParam.setLoginName(param.getLoginName());
		
		validator.validate(newUserParam, bindingResult);
		if(bindingResult.hasErrors()) {
			logger.error("登录名校验未通过。");
			throw new InvalidRequestException(bindingResult);
		}
		
		return new ResponseEntity<Map<String,Object>>(new HashMap<String,Object>(), HttpStatus.OK);
	}
	
	@PutMapping("/user/complete-user-info")
	public ResponseEntity<Map<String, Object>> completeUserInfo(
			@Valid @RequestBody UpdateUserParam param, 
			BindingResult bindingResult) {
		
		Map<String, Object> thirdPartyUser = UserSession.getThirdPartyUser();
		// 如果长时间不操作，存储第三方用户信息的 session 已丢失，则返回 403，让客户端能据此判断。
		if(thirdPartyUser == null) {
			throw new NoAuthorizationException();
		}
		
		UserValidator validator = new UserValidator(userService, propertyService, true);
		NewUserParam newUserParam = new NewUserParam();
		newUserParam.setLoginName(param.getLoginName());
		validator.validate(newUserParam, bindingResult);
		if(bindingResult.hasErrors()) {
			logger.error("登录名校验未通过。");
			throw new InvalidRequestException(bindingResult);
		}
		
		AccountInfo accountInfo = (AccountInfo) thirdPartyUser.get("accountInfo");
		UserInfo userInfo = accountInfo.getUserInfo();
		userInfo.setLoginName(newUserParam.getLoginName().trim());
		UserInfo savedUserInfo = userService.create(userInfo, accountInfo.getUserBind(), accountInfo.getAvatarList());
		
		// 在 security 上下文中存储用户登录信息
		String authorizedClientRegistrationId = (String) thirdPartyUser.get("registrationId");
		// 清除 principal 中的第三方用户信息
		// 在 principal 中添加本地用户信息，至此登录成功
		String token = userService.generateLoginToken(OauthSite.fromValue(authorizedClientRegistrationId), userInfo.getLoginName());
		UserSession.storeUserToSecurityContext(authorizedClientRegistrationId, savedUserInfo, token);
	
		// 用户信息修改完成后，从 session 中删除第三方用户信息
		UserSession.removeThirdPartyUser();
		
		// 返回登录用户信息
		Map<String, Object> user = new HashMap<String, Object>();
		user.put("loginName", savedUserInfo.getLoginName());
		user.put("avatarUrl", savedUserInfo.getAvatarUrl());
		user.put("token", token);
		
		return new ResponseEntity<Map<String, Object>>(user, HttpStatus.OK);
	}
	
	@GetMapping("/user/profile")
	public ResponseEntity<UserInfo> getProfile(Principal principal) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		String loginName = principal.getName();
		return userService.findByLoginName(loginName).map(user -> {
			return ResponseEntity.ok(user);
		}).orElseThrow(NoAuthorizationException::new);
	}
	
	@PutMapping("/user/profile")
	public ResponseEntity<UserInfo> updateProfile(
			@RequestBody UpdateUserParam userParam,
			Principal principal) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Integer userId = userParam.getId();
		UserInfo userInfo = userService.findById(userId).orElseThrow(NoAuthorizationException::new);
		
		userInfo.setNickname(userParam.getNickname());
		userInfo.setWebsiteUrl(userParam.getWebsiteUrl());
		userInfo.setCompany(userParam.getCompany());
		userInfo.setLocation(userParam.getLocation());
		userInfo.setBio(userParam.getBio());
		userInfo.setLastUpdateTime(LocalDateTime.now());
		
		userService.update(userInfo);

		return ResponseEntity.ok(userInfo);
	}
	
}
