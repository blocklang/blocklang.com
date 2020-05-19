package com.blocklang.marketplace.runner.action;

public class PublishedFileInfo {
	private String version;
	private String fileId;
	private String md5sum;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getMd5sum() {
		return md5sum;
	}

	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}

}
