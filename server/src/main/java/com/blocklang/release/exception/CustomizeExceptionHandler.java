package com.blocklang.release.exception;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private MessageSource messageSource;
	
	@ExceptionHandler({ InvalidRequestException.class })
	public ResponseEntity<Object> handleInvalidRequest(InvalidRequestException e, WebRequest request) {
		Locale locale = LocaleContextHolder.getLocale();
		List<String> globalErrors = e.getErrors()
				.getGlobalErrors()
				.stream()
				.map(globalError -> {
					return messageSource.getMessage(globalError, locale);
				})
				.collect(Collectors.toList());
		List<FieldErrorResource> fieldErrorResources = e.getErrors()
				.getFieldErrors()
				.stream()
				.map(fieldError -> new FieldErrorResource(
						fieldError.getObjectName(), 
						fieldError.getField(),
						fieldError.getCode(), 
						messageSource.getMessage(fieldError, locale)))
				.collect(Collectors.toList());

		ErrorResource error = new ErrorResource(globalErrors, fieldErrorResources);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return handleExceptionInternal(e, error, headers, UNPROCESSABLE_ENTITY, request);
	}
}
