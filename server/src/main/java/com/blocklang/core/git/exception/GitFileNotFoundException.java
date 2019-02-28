package com.blocklang.core.git.exception;

/**
 * 根据指定的路径，没有找到git仓库时报错
 * 
 * @author Zhengwei Jin
 *
 */
public class GitFileNotFoundException  extends RuntimeException{

	private static final long serialVersionUID = 3027396229739167333L;
	private String fileName;

	public GitFileNotFoundException(String fileName) {
		super("在git仓库中没有找到文件:" + fileName);
		this.fileName = fileName;
	}
	
	public GitFileNotFoundException(Throwable cause){
		super(cause);
	}

	public GitFileNotFoundException(String fileName, Throwable cause) {
		super("在git仓库中没有找到文件:" + fileName, cause);
		this.fileName = fileName;
	}
	
	public String getFileName(){
		return this.fileName;
	}
}
