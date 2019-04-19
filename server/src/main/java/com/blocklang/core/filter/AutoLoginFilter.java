package com.blocklang.core.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;

import com.blocklang.core.controller.HttpCustomHeader;
import com.blocklang.core.controller.RequestUtil;
import com.blocklang.core.controller.UserSession;
import com.blocklang.core.service.UserService;
import com.blocklang.core.util.LoginToken;
import com.nimbusds.oauth2.sdk.util.StringUtils;

/**
 * 自动登录过滤器
 * 
 * @author Zhengwei Jin
 *
 */
public class AutoLoginFilter implements Filter{
	
	private UserService userService;
	
	public AutoLoginFilter(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		// 只对 fetch 请求
		if(RequestUtil.isFetch(httpRequest) && SecurityContextHolder.getContext().getAuthentication() == null) {
			tryAutoLogin(httpRequest, httpResponse);
		}
		
		chain.doFilter(request, response);
	}
	
	private void tryAutoLogin(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		// 从 header 中获取 token
		String strLogginToken = httpRequest.getHeader(HttpCustomHeader.KEY_LOGGIN_TOLEN);
		if(StringUtils.isNotBlank(strLogginToken)) {
			// 在 token 中包含 provider，格式为 token:provider
			LoginToken loginToken = new LoginToken();
			loginToken.decode(strLogginToken);
			// 在获取的时候，要更新使用时间
			userService.findByLoginToken(loginToken.getToken()).ifPresent(userInfo -> {
				UserSession.storeUserToSecurityContext(loginToken.getProvider(), userInfo, strLogginToken);
			});
		}
	}
}
