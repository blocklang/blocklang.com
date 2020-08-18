package com.blocklang.develop.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.blocklang.core.model.PartialIdField;
import com.blocklang.develop.constant.FileType;
import com.blocklang.develop.constant.converter.FileTypeConverter;

@Entity
@Table(name = "repository_file")
public class RepositoryFile extends PartialIdField{

	private static final long serialVersionUID = 3040976295023993489L;

	@Column(name="repository_resource_id", nullable = false, unique = true, insertable = true, updatable = false)
	private Integer repositoryResourceId;
	
	@Convert(converter = FileTypeConverter.class)
	@Column(name="file_type", nullable = false, length = 2)
	private FileType fileType;
	
	private String content;

	public Integer getRepositoryResourceId() {
		return repositoryResourceId;
	}

	public void setRepositoryResourceId(Integer repositoryResourceId) {
		this.repositoryResourceId = repositoryResourceId;
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
