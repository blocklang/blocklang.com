package com.blocklang.marketplace.data.changelog;

import java.util.List;

/**
 * 一个版本对应一个 changelog 文件
 * 
 * @author Zhengwei Jin
 *
 */
public class ChangeLog {

	private String fileName;
	private String id;
	private String author;
	private String version;
	private List<Change> changes;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<Change> getChanges() {
		return changes;
	}

	public void setChanges(List<Change> changes) {
		this.changes = changes;
	}

}
