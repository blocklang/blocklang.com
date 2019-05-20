package com.blocklang.config;


import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;

import io.sentry.spring.SentryExceptionResolver;
import io.sentry.spring.SentryServletContextInitializer;

@Configuration
public class SentryConfig {

	private Class<?>[] ignoredExceptions = { 
		InvalidRequestException.class,
		ResourceNotFoundException.class,
		NoAuthorizationException.class
	};

	@Bean
	public HandlerExceptionResolver sentryExceptionResolver() {
		return new FilterSentryExceptionResolver();
	}

	@Bean
	public ServletContextInitializer sentryServletContextInitializer() {
		return new SentryServletContextInitializer();
	}

	class FilterSentryExceptionResolver extends SentryExceptionResolver {

		@Override
		public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
			if(e == null) {
				return null;
			}
			if(ignoreReport(e)) {
				return null;
			}
			return super.resolveException(request, response, handler, e);
		}

		private boolean ignoreReport(Exception e) {
			return Arrays.stream(ignoredExceptions).anyMatch(item -> item.isAssignableFrom(e.getClass()));
		}
	}
}
