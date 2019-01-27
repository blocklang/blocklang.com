package com.blocklang.git.exception;

/**
 * 根据指定的路径，没有找到git仓库时报错
 * 
 * @author Zhengwei Jin
 */
public class GitRepoNotFoundException extends RuntimeException{

	private static final long serialVersionUID = -5026335499289061372L;
	private String path;

	public GitRepoNotFoundException(String path) {
		super(path + " is not a valid git repository.");
		this.path = path;
	}
	
	public GitRepoNotFoundException(Throwable cause){
		super(cause);
	}

	public GitRepoNotFoundException(String path, Throwable cause) {
		super(path + " is not a valid git repository.", cause);
		this.path = path;
	}
	
	public String getGitPath(){
		return path;
	}
}
