package com.blocklang.core.git.exception;

public class GitRemoveFailedException extends RuntimeException {

	private static final long serialVersionUID = 808577959637979738L;

	public GitRemoveFailedException(String msg){
		super(msg);
	}

	public GitRemoveFailedException(Throwable cause){
		super(cause);
	}

	public GitRemoveFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
