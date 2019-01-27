package com.blocklang.git.exception;

/**
 * git commit操作失败
 * 
 * @author Zhengwei Jin
 */
public class GitCommitFailedException extends RuntimeException{

	private static final long serialVersionUID = -1977421309661243502L;

	public GitCommitFailedException(String msg){
		super(msg);
	}

	public GitCommitFailedException(Throwable cause){
		super(cause);
	}

	public GitCommitFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
