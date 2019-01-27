package com.blocklang.git.exception;

/**
 * 创建文件或更新文件操作失败
 * 
 * @author Zhengwei Jin
 */
public class FileCreateOrUpdateFailedException extends RuntimeException{

	private static final long serialVersionUID = 1880245484732599166L;
	
	public FileCreateOrUpdateFailedException(String msg){
		super(msg);
	}

	public FileCreateOrUpdateFailedException(Throwable cause){
		super(cause);
	}

	public FileCreateOrUpdateFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
