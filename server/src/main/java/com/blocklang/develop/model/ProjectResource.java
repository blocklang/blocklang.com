package com.blocklang.develop.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
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

	@Column(name = "project_id", nullable = false)
	private Integer projectId;
	
	@Column(name = "resource_key", nullable = false, length = 32)
	private String resourceKey;
	
	@Column(name = "resource_name", nullable = false, length = 32)
	private String resourceName;
	
	@Column(name = "resource_desc", length = 64)
	private String resourceDesc;

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

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getResourceDesc() {
		return resourceDesc;
	}

	public void setResourceDesc(String resourceDesc) {
		this.resourceDesc = resourceDesc;
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
	
}
