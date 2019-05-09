package com.blocklang.core.git.exception;

public class GitEmptyCommitException extends GitCommitFailedException{

	private static final long serialVersionUID = -2709330507711421813L;

	public GitEmptyCommitException(String msg) {
		super(msg);
	}

	public GitEmptyCommitException(Throwable cause){
		super(cause);
	}

	public GitEmptyCommitException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
