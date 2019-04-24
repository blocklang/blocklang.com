package com.blocklang.core.git.exception;

public class GitStatusFailedException extends RuntimeException {

	private static final long serialVersionUID = 4601386988425658852L;

	public GitStatusFailedException(String msg){
		super(msg);
	}

	public GitStatusFailedException(Throwable cause){
		super(cause);
	}

	public GitStatusFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
