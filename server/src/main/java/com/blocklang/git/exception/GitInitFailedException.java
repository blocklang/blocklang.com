package com.blocklang.git.exception;

/**
 * git init操作失败
 * 
 * @author Zhengwei Jin
 */
public class GitInitFailedException extends RuntimeException{

	private static final long serialVersionUID = 6550094866613141523L;

	public GitInitFailedException(String msg){
		super(msg);
	}

	public GitInitFailedException(Throwable cause){
		super(cause);
	}

	public GitInitFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
