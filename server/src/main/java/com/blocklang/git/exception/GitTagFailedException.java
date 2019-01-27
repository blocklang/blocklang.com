package com.blocklang.git.exception;

public class GitTagFailedException extends RuntimeException {

	private static final long serialVersionUID = -9045098569374528199L;

	public GitTagFailedException(String msg){
		super(msg);
	}

	public GitTagFailedException(Throwable cause){
		super(cause);
	}

	public GitTagFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
