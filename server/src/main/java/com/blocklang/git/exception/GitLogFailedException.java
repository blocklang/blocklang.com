package com.blocklang.git.exception;

/**
 * 获取git日志信息失败时抛出的异常
 * 
 * @author Zhengwei Jin
 */
public class GitLogFailedException extends RuntimeException{

	private static final long serialVersionUID = 6530698557862098928L;
	
	public GitLogFailedException(String msg){
		super(msg);
	}

	public GitLogFailedException(Throwable cause){
		super(cause);
	}

	public GitLogFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
