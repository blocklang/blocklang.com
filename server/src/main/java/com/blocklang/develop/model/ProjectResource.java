package com.blocklang.develop.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.constant.converter.AppTypeConverter;
import com.blocklang.develop.constant.converter.ProjectResourceTypeConverter;

@Entity
@Table(name = "project_resource", 
	uniqueConstraints = @UniqueConstraint(columnNames = { "project_id", "resource_key", "resource_type", "app_type", "parent_id" }))
public class ProjectResource extends PartialOperateFields{

	private static final long serialVersionUID = -7591405398132869438L;
	
	/**
	 * 首页
	 */
	public static final String MAIN_KEY = "main";
	public static final String MAIN_NAME = "首页";
	public static final String README_KEY = "README";
	public static final String README_NAME = "README.md";

	@Column(name = "project_id", nullable = false)
	private Integer projectId;
	
	@Column(name = "resource_key", nullable = false, length = 32)
	private String key;
	
	@Column(name = "resource_name", nullable = false, length = 32)
	private String name;
	
	@Column(name = "resource_desc", length = 64)
	private String description;

	@Convert(converter = ProjectResourceTypeConverter.class)
	@Column(name = "resource_type", nullable = false, length = 2)
	private ProjectResourceType resourceType;
	
	@Convert(converter = AppTypeConverter.class)
	@Column(name = "app_type", nullable = false, length = 2)
	private AppType appType;
	
	@Column(name = "parent_id", nullable = false)
	private Integer parentId = Constant.TREE_ROOT_ID;

	@Column(name = "seq", nullable = false)
	private Integer seq;
	
	@Transient
	private String latestCommitId;
	@Transient
	private String latestShortMessage;
	@Transient
	private String latestFullMessage;
	@Transient
	private LocalDateTime latestCommitTime;

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ProjectResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ProjectResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public AppType getAppType() {
		return appType;
	}

	public void setAppType(AppType appType) {
		this.appType = appType;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public String getLatestCommitId() {
		return latestCommitId;
	}

	public void setLatestCommitId(String latestCommitId) {
		this.latestCommitId = latestCommitId;
	}

	public String getLatestShortMessage() {
		return latestShortMessage;
	}

	public void setLatestShortMessage(String latestShortMessage) {
		this.latestShortMessage = latestShortMessage;
	}

	public String getLatestFullMessage() {
		return latestFullMessage;
	}

	public void setLatestFullMessage(String latestFullMessage) {
		this.latestFullMessage = latestFullMessage;
	}

	public LocalDateTime getLatestCommitTime() {
		return latestCommitTime;
	}

	public void setLatestCommitTime(LocalDateTime latestCommitTime) {
		this.latestCommitTime = latestCommitTime;
	}
	
}
