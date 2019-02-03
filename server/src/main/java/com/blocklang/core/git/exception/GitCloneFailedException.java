package com.blocklang.core.git.exception;

public class GitCloneFailedException extends RuntimeException{

	private static final long serialVersionUID = 7164728197351548569L;

	public GitCloneFailedException(String msg){
		super(msg);
	}

	public GitCloneFailedException(Throwable cause){
		super(cause);
	}

	public GitCloneFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
