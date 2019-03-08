package com.blocklang.core.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 *  为了设计用户友好，可读性高的 url，我们将 /projects/{owner}/{project_name} 简写为 /{owner}/{project_name}；
 * 将 /users/{userName} 简写为 /{userName}，因此当请求发过来类似 /{owner}/{project_name} 或 /{userName} 的 url 后，
 * 我们需要在前面补上 /projects 或 /users。
 * 
 * 但是这个操作已经在客户端完成，用户可见的 url 都这样处理了，用户不可见的不做处理，直接使用 /projects/{owner}/{project_name} 这种形式。
 * 
 * @author Zhengwei Jin
 *
 */
public class RouterFilterTest {

	@Test
	public void filter_forward_to_home() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/projects/new");
		request.setServletPath("/projects/new");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl()).isEqualTo("/");
	}
	
	@Test
	public void filter_forward_to_static_1() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/a.js");
		request.setServletPath("/a.js");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain() {

			@Override
			public void doFilter(ServletRequest request, ServletResponse response)
					throws IOException, ServletException {
				request.setAttribute("a", "1");
				super.doFilter(request, response);
			}
			
		};
		assertThat(request.getAttribute("a")).isNull();;
		routerFilter.doFilter(request, response, chain);
		// 即没有做处理，依然是 a.js
		assertThat(response.getForwardedUrl()).isNull();;
		assertThat(request.getAttribute("a")).isEqualTo("1");
	}
	
	@Test
	public void filter_forward_to_static_2() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/projects/a.js");
		request.setServletPath("/projects/a.js");
		request.addHeader("referer", "/projects/new");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl()).isEqualTo("/a.js");
	}
	
	@Test
	public void filter_forward_to_static_3() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/projects/src/a.js");
		request.addHeader("referer", "/projects/new");
		request.setServletPath("/projects/src/a.js");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl()).isEqualTo("/src/a.js");
	}
	
	@Test
	public void filter_forward_to_static_4() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/src/a.js");
		request.setServletPath("/src/a.js");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain() {

			@Override
			public void doFilter(ServletRequest request, ServletResponse response)
					throws IOException, ServletException {
				request.setAttribute("a", "1");
				super.doFilter(request, response);
			}
			
		};
		assertThat(request.getAttribute("a")).isNull();;
		routerFilter.doFilter(request, response, chain);
		// 即没有做处理，依然是 /src/a.js
		assertThat(response.getForwardedUrl()).isNull();;
		assertThat(request.getAttribute("a")).isEqualTo("1");
	}
	
	@Test
	public void filter_forward_to_static_5() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/jack/a.js");
		request.addHeader("referer", "/jack/my-project");
		request.setServletPath("/jack/a.js");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl()).isEqualTo("/a.js");
	}
	
	@Test
	public void filter_forward_to_static_6() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/jack/my-project/a.js");
		request.addHeader("referer", "/jack/my-project/tree");
		request.setServletPath("/jack/my-project/a.js");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl()).isEqualTo("/a.js");
	}
	
	@Test
	public void filter_get_js_source_map() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/projects/a.js");
		request.setServletPath("/projects/a.js");
		request.addHeader("referer", "/projects/new");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		
		request = new MockHttpServletRequest("GET", "/projects/a.js.map");
		request.setServletPath("/projects/a.js.map");
		response = new MockHttpServletResponse();
		chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		
		assertThat(response.getForwardedUrl()).isEqualTo("/a.js.map");
	}
	
	// 注意：文件为 source map 时，referer 的值为 null
	@Test
	public void filter_get_css_source_map() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/projects/a.css");
		request.setServletPath("/projects/a.css");
		request.addHeader("referer", "/projects/new");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		
		request = new MockHttpServletRequest("GET", "/projects/a.css.map");
		request.setServletPath("/projects/a.css.map");
		response = new MockHttpServletResponse();
		chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		
		assertThat(response.getForwardedUrl()).isEqualTo("/a.css.map");
	}
	
	@Test
	public void filter_forward_to_user_servlet() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user");
		request.setServletPath("/user");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl()).isNull();;
	}
	
	@Test
	public void filter_owner_project_to_home() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/jack/my-project");
		request.setServletPath("/jack/my-project");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl()).isEqualTo("/");
	}
	
	@Test
	public void filter_owner_project_with_path_to_home() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/jack/my-project/a/b");
		request.setServletPath("/jack/my-project/a/b");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl()).isEqualTo("/");
	}
	
	@Test
	public void filter_users_to_home() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/jack");
		request.setServletPath("/jack");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl()).isEqualTo("/");
	}
}
