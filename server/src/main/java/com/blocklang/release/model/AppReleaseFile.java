package com.blocklang.release.model;

import java.io.File;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.TargetOs;
import com.blocklang.release.constant.converter.ArchConverter;
import com.blocklang.release.constant.converter.TargetOsConverter;

@Entity
public class AppReleaseFile extends PartialOperateFields{

	private static final long serialVersionUID = -8705804993013969516L;
	
	@Column(name = "app_release_id", nullable = false)
	private Integer appReleaseId;
	
	@Column(name = "target_os", nullable = false, length = 2)
	@Convert(converter = TargetOsConverter.class)
	private TargetOs targetOs;
	
	@Column(name = "arch", nullable = false, length = 2)
	@Convert(converter = ArchConverter.class)
	private Arch arch;
	
	@Column(name = "file_name", nullable = false)
	private String fileName;
	
	@Column(name = "file_path", nullable = false)
	private String filePath;
	
	@Transient
	private String absoluteRootPath;

	public Integer getAppReleaseId() {
		return appReleaseId;
	}

	public void setAppReleaseId(Integer appReleaseId) {
		this.appReleaseId = appReleaseId;
	}

	public TargetOs getTargetOs() {
		return targetOs;
	}

	public void setTargetOs(TargetOs targetOs) {
		this.targetOs = targetOs;
	}

	public Arch getArch() {
		return arch;
	}

	public void setArch(Arch arch) {
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

	public String getFullPath() {
		return absoluteRootPath + File.separator + filePath;
	}
	
	public void setAbsoluteRootPath(String absoluteRootPath) {
		this.absoluteRootPath = absoluteRootPath;
	}

}
