package com.blocklang.core.filter;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class RouterFilterTest {

	@Test
	public void filter_forward_to_home() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/projects/new");
		request.setServletPath("/projects/new");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl(), equalTo("/"));
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
		assertThat(request.getAttribute("a"), is(nullValue()));
		routerFilter.doFilter(request, response, chain);
		// 即没有做处理，依然是 a.js
		assertThat(response.getForwardedUrl(), is(nullValue()));
		assertThat(request.getAttribute("a"), equalTo("1"));
	}
	
	@Test
	public void filter_forward_to_static_2() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/projects/a.js");
		request.setServletPath("/projects/a.js");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl(), equalTo("/a.js"));
	}
	
	@Test
	public void filter_forward_to_static_3() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/projects/src/a.js");
		request.setServletPath("/projects/src/a.js");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl(), equalTo("/src/a.js"));
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
		assertThat(request.getAttribute("a"), is(nullValue()));
		routerFilter.doFilter(request, response, chain);
		// 即没有做处理，依然是 /src/a.js
		assertThat(response.getForwardedUrl(), is(nullValue()));
		assertThat(request.getAttribute("a"), equalTo("1"));
	}
	
	@Test
	public void filter_forward_to_user_servlet() throws IOException, ServletException {
		RouterFilter routerFilter = new RouterFilter();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user");
		request.setServletPath("/user");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		routerFilter.doFilter(request, response, chain);
		assertThat(response.getForwardedUrl(), is(nullValue()));
	}
}
