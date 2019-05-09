package com.blocklang.develop.data;

import javax.validation.constraints.NotBlank;

public class CommitMessage {

	@NotBlank(message = "{NotBlank.commitMessage}")
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
