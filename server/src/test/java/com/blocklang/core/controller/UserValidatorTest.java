package com.blocklang.core.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.blocklang.core.data.NewUserParam;
import com.blocklang.core.model.CmProperty;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.UserService;

public class UserValidatorTest {

	@Test
	public void valid_login_name_should_not_empty() {
		UserValidator validator = new UserValidator(null, null, false);
		
		NewUserParam param = new NewUserParam();
		Map<String, String> map = new HashMap<String, String>();
		Errors errors = new MapBindingResult(map, NewUserParam.class.getName());
		validator.validate(param, errors);
		
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getFieldError("loginName").getCode()).isEqualTo("NotBlank.loginName");
	}
	
	// 不能以中划线开头
	@Test
	public void valid_login_name_should_not_starts_with_line_through() {
		UserValidator validator = new UserValidator(null, null, false);
		
		NewUserParam param = new NewUserParam();
		param.setLoginName("-a");
		
		Map<String, String> map = new HashMap<String, String>();
		Errors errors = new MapBindingResult(map, NewUserParam.class.getName());
		validator.validate(param, errors);
		
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getFieldError("loginName").getCode()).isEqualTo("NotValid.startsWithLetterOrNumber");
	}
	
	// 不能以下划线开头
	@Test
	public void valid_login_name_should_not_starts_with_underline() {
		UserValidator validator = new UserValidator(null, null, false);
		
		NewUserParam param = new NewUserParam();
		param.setLoginName("_a");
		
		Map<String, String> map = new HashMap<String, String>();
		Errors errors = new MapBindingResult(map, NewUserParam.class.getName());
		validator.validate(param, errors);
		
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getFieldError("loginName").getCode()).isEqualTo("NotValid.startsWithLetterOrNumber");
	}
	
	// 不能以中划线结尾
	@Test
	public void valid_login_name_should_not_ends_with_line_through() {
		UserValidator validator = new UserValidator(null, null, false);
		
		NewUserParam param = new NewUserParam();
		param.setLoginName("a-");
		
		Map<String, String> map = new HashMap<String, String>();
		Errors errors = new MapBindingResult(map, NewUserParam.class.getName());
		validator.validate(param, errors);
		
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getFieldError("loginName").getCode()).isEqualTo("NotValid.endsWithLetterOrNumber");
	}
	
	// 不能以下划线结尾
	@Test
	public void valid_login_name_should_not_ends_with_underline() {
		UserValidator validator = new UserValidator(null, null, false);
		
		NewUserParam param = new NewUserParam();
		param.setLoginName("a_");
		
		Map<String, String> map = new HashMap<String, String>();
		Errors errors = new MapBindingResult(map, NewUserParam.class.getName());
		validator.validate(param, errors);
		
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getFieldError("loginName").getCode()).isEqualTo("NotValid.endsWithLetterOrNumber");
	}
	
	// 只能包含字母、数字、下划线(_)或中划线(-)
	@Test
	public void valid_login_name_should_not_contains_chinese() {
		UserValidator validator = new UserValidator(null, null, false);
		
		NewUserParam param = new NewUserParam();
		param.setLoginName("a中文b");
		
		Map<String, String> map = new HashMap<String, String>();
		Errors errors = new MapBindingResult(map, NewUserParam.class.getName());
		validator.validate(param, errors);
		
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getFieldError("loginName").getCode()).isEqualTo("NotValid.onlyContainsLetterOrNumberOrUnderlineOrLineThrough");
	}
	
	// 用户第一次登录时，要校验用户名是否存在
	@Test
	public void valid_login_name_is_used_when_first_login() {
		UserService userService = mock(UserService.class);
		UserInfo user = new UserInfo();
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		UserValidator validator = new UserValidator(userService, null, true);
		
		NewUserParam param = new NewUserParam();
		param.setLoginName("a");
		
		Map<String, String> map = new HashMap<String, String>();
		Errors errors = new MapBindingResult(map, NewUserParam.class.getName());
		validator.validate(param, errors);
		
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getFieldError("loginName").getCode()).isEqualTo("NotValid.loginNameIsUsed");
	}
	
	@Test
	public void valid_login_name_should_not_be_keywords() {
		PropertyService propertyService = mock(PropertyService.class);
		
		List<CmProperty> keywords = new ArrayList<CmProperty>();
		CmProperty keyword1 = new CmProperty();
		keyword1.setValue("keyword_1");
		keywords.add(keyword1);
		when(propertyService.findAllByParentKey(anyString())).thenReturn(keywords);
		UserValidator validator = new UserValidator(null, propertyService, false);
		
		NewUserParam param = new NewUserParam();
		param.setLoginName("keyword_1");
		
		Map<String, String> map = new HashMap<String, String>();
		Errors errors = new MapBindingResult(map, NewUserParam.class.getName());
		validator.validate(param, errors);
		
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getFieldError("loginName").getCode()).isEqualTo("NotValid.loginNameIsUsed");
	}
	
}
