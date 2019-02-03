package com.blocklang.core.git.exception;

/**
 * git pull 失败
 * 
 * @author Zhengwei Jin
 */
public class GitPullFailedException extends RuntimeException{

	private static final long serialVersionUID = 6530698557862098928L;
	
	public GitPullFailedException(String msg){
		super(msg);
	}

	public GitPullFailedException(Throwable cause){
		super(cause);
	}

	public GitPullFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}