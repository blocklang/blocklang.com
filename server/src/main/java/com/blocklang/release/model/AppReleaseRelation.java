package com.blocklang.release.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name = "app_release_relation", 
	uniqueConstraints = @UniqueConstraint(columnNames = { "app_release_id", "depend_app_release_id" }))
public class AppReleaseRelation extends PartialIdField {

	private static final long serialVersionUID = 2648941118207608318L;

	@Column(name = "app_release_id", nullable = false)
	private Integer appReleaseId;

	@Column(name = "depend_app_release_id", nullable = false)
	private Integer dependAppReleaseId;

	public Integer getAppReleaseId() {
		return appReleaseId;
	}

	public void setAppReleaseId(Integer appReleaseId) {
		this.appReleaseId = appReleaseId;
	}

	public Integer getDependAppReleaseId() {
		return dependAppReleaseId;
	}

	public void setDependAppReleaseId(Integer dependAppReleaseId) {
		this.dependAppReleaseId = dependAppReleaseId;
	}

}
