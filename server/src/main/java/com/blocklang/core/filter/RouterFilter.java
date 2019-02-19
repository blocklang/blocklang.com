package com.blocklang.core.filter;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriUtils;

import com.blocklang.core.constant.WebSite;


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

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String servletPath = httpServletRequest.getServletPath();
		String url = httpServletRequest.getRequestURI();
		if(StringUtils.isBlank(servletPath)) {
			servletPath = url;
		}
		// Single Page Application 单页面应用的路由处理
		System.out.println("===============================================");
		System.out.println("===============================================");
		System.out.println("===============================================");
		System.out.println("servlet path = " + servletPath);
		httpServletRequest.getHeaderNames().asIterator().forEachRemaining(name -> System.out.println(name));
		
		System.out.println("host:" + httpServletRequest.getHeader("host"));
		System.out.println("referer:" + httpServletRequest.getHeader("referer"));
		System.out.println("url:" + url);
		System.out.println("context path:" + httpServletRequest.getContextPath());
		System.out.println("pathInfo:" + httpServletRequest.getPathInfo());
		
		// 当按下浏览器的 F5，刷新 Single Page Application 的任一页面时，都跳转到首页
		if(ArrayUtils.contains(Resources.ROUTES, servletPath)) {
			request.getRequestDispatcher(WebSite.HOME_URL).forward(request, response);
			return;
		}
		
		String filenameExtension = UriUtils.extractFileExtension(servletPath);
		if(StringUtils.isNotBlank(filenameExtension) && ArrayUtils.contains(Resources.PUBLIC_FILE_EXTENSIONS, filenameExtension)) {
			// 注意：使用 StringUtils.split 会剔除第一个数值为空字符串的元素
			String[] pathSegment = StringUtils.split(servletPath, "/");
			
			// 去除掉 servletPath 中的 servlet name 部分
			boolean hasServlet = false;
			// 1. 先判断是否存在 servlet
			if(pathSegment.length > 0) {
				String servlet = pathSegment[0];
				hasServlet = ArrayUtils.contains(Resources.SERVLET_NAMES, servlet);
			}
			
			// 2. 移除 servlet 名
			if(hasServlet && pathSegment.length > 1) {
				String[] trimedPathSegment = Arrays.copyOfRange(pathSegment, 1, pathSegment.length);
				String trimedPath = String.join("/", trimedPathSegment);
				request.getRequestDispatcher("/" + trimedPath).forward(request, response);
				return;
			}
		}
		
		if(StringUtils.isBlank(filenameExtension) && needPrependServlet(servletPath)) {
			String newUrl = "";
			
			// 确保 servletPath 以 / 开头，是否默认都已经以 / 开头了呢？
			// TODO: 如果一个版本之后，程序不会出错，则删除此段注释掉的代码
//			if(!servletPath.startsWith("/")) {
//				servletPath = "/" + servletPath;
//			}
			
			if(StringUtils.split(servletPath, "/").length == 1) {// 如 “/zhangsan”
				newUrl = "/users" + servletPath;
			}else{ // 一段的都留给 users，大于等于两段的都是 projects 的，如 “/zhangsan/my-project”
				newUrl = "/projects" + servletPath;
			}
			
			request.getRequestDispatcher(newUrl).forward(request, response);
			return;
		}
		
		chain.doFilter(request, response);
	}

	/**
	 * 判断是否需要在 servletPath 前追加 servlet 名
	 * 
	 * 为了设计用户友好，可读性高的 url，我们将 /projects/{owner}/{project_name} 简写为 /{owner}/{project_name}；
	 * 将 /users/{userName} 简写为 /{userName}，因此当请求发过来类似 /{owner}/{project_name} 或 /{userName} 的 url 后，
	 * 我们需要在前面补上 /projects 或 /users
	 * 
	 * @param servletPath
	 * @return 如果需要增补，则返回 <code>true</code>；否则返回 <code>false</code>。
	 */
	private boolean needPrependServlet(String servletPath) {
		if (StringUtils.equals("/", servletPath)) {
			return false;
		}
		if (!servletPath.startsWith("/")) {
			servletPath = "/" + servletPath;
		}
		// 精准判断 servlet path
		String[] pathSegment = servletPath.split("/");
		// 索引为 0 的值是空字符串，所以这里取第二个值
		String firstPath = pathSegment[1];
		for (String each : Resources.SERVLET_NAMES) {
			if (firstPath.equals(each)) {
				return false;
			}
		}
		return true;
	}

}
