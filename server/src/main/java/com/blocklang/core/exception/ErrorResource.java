package com.blocklang.core.exception;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ErrorResourceSerializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("errors")
public class ErrorResource {
	private List<String> globalErrors;
	private List<FieldErrorResource> fieldErrors;

	public ErrorResource(List<String> globalErrors, List<FieldErrorResource> fieldErrorResources) {
		this.globalErrors = globalErrors;
		this.fieldErrors = fieldErrorResources;
	}

	public List<FieldErrorResource> getFieldErrors() {
		return fieldErrors;
	}

	public List<String> getGlobalErrors() {
		return globalErrors;
	}

}
