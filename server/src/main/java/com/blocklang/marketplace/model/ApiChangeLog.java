package com.blocklang.marketplace.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;
import com.blocklang.marketplace.constant.ChangelogExecuteResult;
import com.blocklang.marketplace.constant.converter.ChangelogExecuteResultConverter;

@Entity
@Table(name="api_changelog",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"api_repo_id", "changelog_id", "changelog_author", "changelog_file_name"})
	}
)
public class ApiChangeLog extends PartialIdField {

	private static final long serialVersionUID = -3946939101654808302L;
	
	@Column(name = "api_repo_id", nullable = false)
	private Integer apiRepoId;
	
	@Column(name = "changelog_id", nullable = false, length = 255)
	private String changelogId;
	
	@Column(name = "changelog_author", nullable = false, length = 255)
	private String changelogAuthor;
	
	@Column(name = "changelog_file_name", nullable = false, length = 255)
	private String changelogFileName;
	
	@Column(name = "execute_time", insertable = true, updatable = false, nullable = false)
	private LocalDateTime executeTime;
	
	@Column(name = "execute_order", nullable = false)
	private Integer executeOrder;
	
	@Convert(converter = ChangelogExecuteResultConverter.class)
	@Column(name = "execute_result", nullable = false, length = 2)
	private ChangelogExecuteResult executeResult;
	
	@Column(name = "md5_sum", nullable = false, length = 64)
	private String md5Sum;
	
	@Column(name = "deployment_id", nullable = false)
	private Integer deploymentId;
	
	@Column(name = "create_user_id", insertable = true, updatable = false, nullable = false)
	private Integer createUserId;
	
	@Column(name = "create_time", insertable = true, updatable = false, nullable = false)
	private LocalDateTime createTime;

	public Integer getApiRepoId() {
		return apiRepoId;
	}

	public void setApiRepoId(Integer apiRepoId) {
		this.apiRepoId = apiRepoId;
	}

	public String getChangelogId() {
		return changelogId;
	}

	public void setChangelogId(String changelogId) {
		this.changelogId = changelogId;
	}

	public String getChangelogAuthor() {
		return changelogAuthor;
	}

	public void setChangelogAuthor(String changelogAuthor) {
		this.changelogAuthor = changelogAuthor;
	}

	public String getChangelogFileName() {
		return changelogFileName;
	}

	public void setChangelogFileName(String changelogFileName) {
		this.changelogFileName = changelogFileName;
	}

	public LocalDateTime getExecuteTime() {
		return executeTime;
	}

	public void setExecuteTime(LocalDateTime executeTime) {
		this.executeTime = executeTime;
	}

	public Integer getExecuteOrder() {
		return executeOrder;
	}

	public void setExecuteOrder(Integer executeOrder) {
		this.executeOrder = executeOrder;
	}

	public ChangelogExecuteResult getExecuteResult() {
		return executeResult;
	}

	public void setExecuteResult(ChangelogExecuteResult executeResult) {
		this.executeResult = executeResult;
	}

	public String getMd5Sum() {
		return md5Sum;
	}

	public void setMd5Sum(String md5Sum) {
		this.md5Sum = md5Sum;
	}

	public Integer getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(Integer deploymentId) {
		this.deploymentId = deploymentId;
	}

	public Integer getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(Integer createUserId) {
		this.createUserId = createUserId;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}
	
}
