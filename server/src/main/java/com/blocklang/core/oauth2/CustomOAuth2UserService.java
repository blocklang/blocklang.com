package com.blocklang.core.oauth2;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.controller.UserSession;
import com.blocklang.core.controller.UserValidator;
import com.blocklang.core.data.AccountInfo;
import com.blocklang.core.data.NewUserParam;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.oauth2.qq.QqOauth2UserService;
import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.QqLoginService;
import com.blocklang.core.service.UserBindService;
import com.blocklang.core.service.UserService;

public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>{

	private DefaultOAuth2UserService defaultOAuth2UserService;
	private QqOauth2UserService qqUserService;
	
	private GithubLoginService githubLoginService;
	private QqLoginService qqLoginService;

	private UserService userService;
	private UserBindService userBindService;
	private PropertyService propertyService;
	
	private MessageSource messageSource;
	
	public CustomOAuth2UserService(
			GithubLoginService githubLoginService,
			QqLoginService qqLoginService,
			
			UserService userService,
			UserBindService userBindService,
			PropertyService propertyService,
			MessageSource messageSource) {
		defaultOAuth2UserService = new DefaultOAuth2UserService();
		qqUserService = new QqOauth2UserService();
		
		this.githubLoginService = githubLoginService;
		this.qqLoginService = qqLoginService;
		
		this.userService = userService;
		this.userBindService = userBindService;
		this.propertyService = propertyService;
		
		this.messageSource = messageSource;
	}
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		
		// 获取第三方网站的用户信息
		// 将第三方网站的用户信息转换为本网站用户信息格式
		OAuth2User oauthUser = null;
		OauthSite site = null;
		String loginName = null;
		AccountInfo accountInfo = null;
		if(registrationId.equalsIgnoreCase(OauthSite.GITHUB.getValue())) {
			try {
				oauthUser = defaultOAuth2UserService.loadUser(userRequest);
			} catch (OAuth2AuthenticationException e) {
				e.printStackTrace();
				throw e;
			}
			site = OauthSite.GITHUB;
			loginName = Objects.toString(oauthUser.getAttributes().get("login"), null);
			accountInfo = githubLoginService.getThirdPartyUser(oauthUser);
		}else if(registrationId.equalsIgnoreCase(OauthSite.QQ.getValue())) {
			oauthUser = qqUserService.loadUser(userRequest);
			site = OauthSite.QQ;
			accountInfo = qqLoginService.getThirdPartyUser(oauthUser);
		}
		
		if(oauthUser == null) {
			return null;
		}
		
		UserInfo userInfo = null;
		// 判断第三方网站的用户是否绑定过
		String openId = oauthUser.getName();
		Optional<UserBind> userBindOption = userBindService.findBySiteAndOpenId(site, openId);
		boolean isFirstLogin = userBindOption.isEmpty();
		if(isFirstLogin) {
			// 用户登录名是 blocklang 网站专用的，第一次可以从第三方网站获取，
			// 但是之后就不能再通过同步第三方网站信息来修改登录名。
			
			// 只有在第一次登录时才校验用户登录名，因为在登录阶段，用户登录名不允许修改。
			// 校验用户信息
			
			// 校验用户登录名是否符合命名规范，如果不符合则跳转到完善信息页面。
			// 也要之前登录成功的用户做校验，以防在添加此校验逻辑之前注册的用户不符合校验规则。
			
			// 如果无效, 则临时存储第三方用户信息，并跳转到完善用户信息页面（在此页面中完成保存用户信息的操作）
			// 如果有效，则将用户信息存储到数据库中，并直接跳转到用户个人首页
			
			UserValidator userValidator = new UserValidator(userService, propertyService, isFirstLogin);
			Map<String, String> map = new HashMap<String, String>();
			Errors errors = new MapBindingResult(map, NewUserParam.class.getName());
			NewUserParam param = new NewUserParam();
			param.setLoginName(loginName);
			userValidator.validate(param, errors);
			
			if(errors.hasErrors()) {
				System.out.println("校验失败，并跳转到完善用户信息页面。");
				String code = errors.getFieldError("loginName").getCode();
				System.out.println("error code: " + code);
				
				// 将第三方用户信息转换为本网站的用户信息
				// 这里主要是存储用户的昵称和最小尺寸的头像，在完善用户信息页面使用
				Map<String, Object> userAttributes = new HashMap<String, Object>();
				userAttributes.put("accountInfo", accountInfo);
				Locale locale = LocaleContextHolder.getLocale();
				userAttributes.put("loginNameErrorMessage", messageSource.getMessage(code, new Object[] {},locale));
				userAttributes.put("registrationId", registrationId);
				UserSession.setThirdPartyUser(userAttributes);
				
				// 抛出异常，是为了告知登录失败，不在 security 中存储用户信息
				throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED));
			}
			
			// 第一次登录校验通过，则保存用户信息
			userInfo = userService.create(accountInfo.getUserInfo(), accountInfo.getUserBind(), accountInfo.getAvatarList());
		}else {
			Integer localUserId = userBindOption.get().getUserId();
			userInfo = userService.update(localUserId, accountInfo.getUserInfo(), accountInfo.getAvatarList(), "loginName");
		}
		
		if(userInfo != null) {
			// 将第三方用户信息转换为本网站的用户信息
			// 这里主要是存储用户 id 等页面上常用信息
			Map<String, Object> userAttributes = new HashMap<String, Object>();
			userAttributes.put("id", userInfo.getId());
			userAttributes.put("loginName", userInfo.getLoginName());
			userAttributes.put("avatarUrl", userInfo.getAvatarUrl());
			userAttributes.put("token", userService.generateLoginToken(OauthSite.fromValue(registrationId), userInfo.getLoginName()));
			// 经过反复考虑，这里设置 loginName 而不是设置 id
			// 首先将 loginName 加上唯一约束后，使用 byLoginName 和 byId 获取用户信息的效果是一样的
			// 这样在写代码时，不要做各种转换
			return new DefaultOAuth2User(oauthUser.getAuthorities(), userAttributes, "loginName");
		}

		return oauthUser;
	}

}