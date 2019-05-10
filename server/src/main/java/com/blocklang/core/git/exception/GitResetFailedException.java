package com.blocklang.core.git.exception;

public class GitResetFailedException extends RuntimeException {

	private static final long serialVersionUID = 808577959637979738L;

	public GitResetFailedException(String msg){
		super(msg);
	}

	public GitResetFailedException(Throwable cause){
		super(cause);
	}

	public GitResetFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
