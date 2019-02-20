package com.blocklang.develop.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.blocklang.core.model.PartialIdField;
import com.blocklang.develop.constant.FileType;
import com.blocklang.develop.constant.converter.FileTypeConverter;

@Entity
@Table(name = "project_file")
public class ProjectFile extends PartialIdField{

	private static final long serialVersionUID = 3040976295023993489L;

	@Column(name="project_resource_id", nullable = false, unique = true, insertable = true, updatable = false)
	private Integer projectResourceId;
	
	@Convert(converter = FileTypeConverter.class)
	@Column(name="file_type", nullable = false, length = 2)
	private FileType fileType;
	
	private String content;

	public Integer getProjectResourceId() {
		return projectResourceId;
	}

	public void setProjectResourceId(Integer projectResourceId) {
		this.projectResourceId = projectResourceId;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
