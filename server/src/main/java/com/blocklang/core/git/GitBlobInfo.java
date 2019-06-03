package com.blocklang.core.git;

public class GitBlobInfo extends GitFileInfo{
	
	private String content;

	public GitBlobInfo() {
		super();
		super.setFolder(false);
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
