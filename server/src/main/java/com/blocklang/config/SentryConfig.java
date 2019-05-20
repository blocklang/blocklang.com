package com.blocklang.config;

import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import io.sentry.spring.SentryExceptionResolver;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Configuration
public class SentryConfig {
    private Class<?>[] ignoredExceptions = { InvalidRequestException.class, ResourceNotFoundException.class, NoAuthorizationException.class };

    @Bean
    public HandlerExceptionResolver sentryExceptionResolver() {
        return new FilterSentryExceptionResolver();
    }

    @Bean
    public ServletContextInitializer sentryServletContextInitializer() {
        return new io.sentry.spring.SentryServletContextInitializer();
    }

    public class FilterSentryExceptionResolver extends SentryExceptionResolver{
        @Override
        public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            if(ex != null){
                if(needReport(ex)){
                    return super.resolveException(request, response, handler, ex);
                }
            }
            return null;
        }

        private boolean needReport(Exception e){
            for(Class<?> clazz : ignoredExceptions){
                if(!Objects.isNull(clazz) && clazz.isAssignableFrom(e.getClass())){
                    return false;
                }
            }
            return true;
        }
    }
}
