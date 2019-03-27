package com.blocklang.core.controller;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.data.NewUserParam;
import com.blocklang.core.model.CmProperty;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.UserService;
import com.nimbusds.oauth2.sdk.util.StringUtils;

public class UserValidator implements Validator{

	private UserService userService;
	private PropertyService propertyService;
	private boolean isFirstLogin;
	
	public UserValidator(UserService userService, PropertyService propertyService, boolean isFirstLogin) {
		this.userService = userService;
		this.propertyService = propertyService;
		this.isFirstLogin = isFirstLogin;
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		return NewUserParam.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		NewUserParam param = (NewUserParam)target;
		
		// 校验登录名
		String loginName = param.getLoginName();
		// 1. 不能为空
		if(StringUtils.isBlank(loginName)) {
			errors.rejectValue("loginName", "NotBlank.loginName");
			return;
		}
		
		// 2. 登录名只能包含：英文字母、数字、中划线（-）或下划线（_），只能以英文字母或数字开头或结尾，不区分大小写
		// 2.1 只能以字母或数字开头
		String reg = "^[a-zA-Z0-9].*?";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(loginName);
		if(!matcher.matches()) {
			errors.rejectValue("loginName", "NotValid.startsWithLetterOrNumber");
			return;
		}
		
		// 2.2 只能以字母或数字结尾
		reg = ".*?[a-zA-Z0-9]$";
		pattern = Pattern.compile(reg);
		matcher = pattern.matcher(loginName);
		if(!matcher.matches()){
			errors.rejectValue("loginName", "NotValid.endsWithLetterOrNumber");
			return;
		}
		
		// 2.3 只能包含字母、数字、下划线(_)或中划线(-)
		reg = "^[a-zA-Z0-9\\w\\-]{1,32}$";
		pattern = Pattern.compile(reg);
		matcher = pattern.matcher(loginName);
		if(!matcher.matches()){
			errors.rejectValue("loginName", "NotValid.onlyContainsLetterOrNumberOrUnderlineOrLineThrough");
			return;
		}
		
		// 3. 校验用户名是否存在
		// 校验规则为：
		// 3.1 当用户是第一次登录时，校验用户名是否存在
		// 3.2 当用户已成功登录过时（用户信息已入库），则要先排除自己，然后再校验
		// 因为可能会出现用户在第三方平台修改了登录名，而这个登录名在 blocklang 平台被占用
		// 如果这个名字是自己使用的，则校验通过
		// 为了逻辑简单清晰，约定：当用户已成功登录过后，则不再同步登录名，不论用户是否在第三方平台修改过登录名。
		// 所以不用再做 3.2 校验，只有在第一次登录时才校验
		if(this.isFirstLogin) {
			if(this.userService.findByLoginName(loginName).isPresent()) {
				errors.rejectValue("loginName", "NotValid.loginNameIsUsed");
				return;
			};
		}
		
		// 4. 不能使用平台中的关键字（或叫保留字）做用户名
		List<CmProperty> keywords = this.propertyService.findAllByParentKey(CmPropKey.PLATFORM_KEYWORDS);
		if(keywords.stream().anyMatch(cmProperty -> {return cmProperty.getValue().equalsIgnoreCase(loginName);})) {
			errors.rejectValue("loginName", "NotValid.loginNameIsUsed");
			return;
		}
	}
	
	
}
