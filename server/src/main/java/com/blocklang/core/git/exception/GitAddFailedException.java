package com.blocklang.core.git.exception;

public class GitAddFailedException extends RuntimeException {

	private static final long serialVersionUID = 4601386988425658852L;

	public GitAddFailedException(String msg){
		super(msg);
	}

	public GitAddFailedException(Throwable cause){
		super(cause);
	}

	public GitAddFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

