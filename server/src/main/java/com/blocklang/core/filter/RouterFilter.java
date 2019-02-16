package com.blocklang.core.filter;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UriUtils;

/**
 * URL 转换过滤器
 * 
 * <p>
 * 将用户友好的 URL 转换为 RESTful 风格的 URL。 如将 "/{owner}/{projectName}" 转换为
 * "/projects/{owner}/{projectName}" 
 * 注意：转换的经过 forward 跳转的 url 不会被 security
 * 拦截，即判断用户是否登录的权限控制需要单独处理
 * 
 * @author jinzw
 *
 */
// 已在 SecurityConfig 中手动注入该 Filter，在此处不需使用 @Component("routerFilter") 方式注入

public class RouterFilter implements Filter{

	private static final String[] INCLUDE_FILE_EXTENSIONS = new String[] {"js","css","map","woff2","ttf", "eot", "svg", "woff", "json"};
	private static final String[] SERVLET = new String[] {"projects"};
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String servletPath = httpServletRequest.getServletPath();
		// Single Page Application 单页面应用的路由处理
		System.out.println("===============================================");
		System.out.println("===============================================");
		System.out.println("===============================================");
		System.out.println("servlet path = " + servletPath);
		httpServletRequest.getHeaderNames().asIterator().forEachRemaining(name -> System.out.println(name));
		
		System.out.println("host:" + httpServletRequest.getHeader("host"));
		System.out.println("referer:" + httpServletRequest.getHeader("referer"));
		String url = httpServletRequest.getRequestURI();
		System.out.println("url:" + url);
		
		// 当按下浏览器的 F5，刷新 Single Page Application 的任一页面时，都跳转到首页
		if(servletPath.equals("/projects/new")) {
			request.getRequestDispatcher("/").forward(request, response);
			return;
		}
		
		String filenameExtension = UriUtils.extractFileExtension(servletPath);
		if(filenameExtension != null && 
				Arrays.stream(INCLUDE_FILE_EXTENSIONS).anyMatch(item -> item.equals(filenameExtension))) {
			String[] pathSegment = servletPath.split("/");
			
			boolean hasServlet = false;
			if(pathSegment.length > 1) {
				String servlet = pathSegment[1];
				hasServlet = Arrays.stream(SERVLET).anyMatch(item -> item.equals(servlet));
			}
			
			if(hasServlet && pathSegment.length > 2) {
				String[] trimedPathSegment = Arrays.copyOfRange(pathSegment, 2, pathSegment.length);
				String trimedPath = String.join("/", trimedPathSegment);
				
				request.getRequestDispatcher("/" + trimedPath).forward(request, response);
				return;
			}
		}
		
		chain.doFilter(request, response);
	}

}
