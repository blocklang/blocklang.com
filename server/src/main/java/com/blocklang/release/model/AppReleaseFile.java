package com.blocklang.release.model;

public class AppReleaseFile {
	private Integer id;
	private Integer appReleaseId;
	private String targetOs;
	private String arch;
	private String fileName;
	private String filePath;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAppReleaseId() {
		return appReleaseId;
	}

	public void setAppReleaseId(Integer appReleaseId) {
		this.appReleaseId = appReleaseId;
	}

	public String getTargetOs() {
		return targetOs;
	}

	public void setTargetOs(String targetOs) {
		this.targetOs = targetOs;
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
