package com.blocklang.develop.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name = "repository_commit",
	uniqueConstraints = @UniqueConstraint(columnNames = { "repository_id", "branch", "commit_id" }))
public class RepositoryCommit extends PartialIdField{

	private static final long serialVersionUID = 1597122472443220475L;
	
	@Column(name = "repository_id", nullable = false)
	private Integer repositoryId;
	
	@Column(name = "branch", length = 32, nullable = false)
	private String branch;
	
	@Column(name = "commit_id", length = 128, nullable = false)
	private String commitId;
	
	@Column(name = "commit_user_id", nullable = false)
	private Integer commitUserId;
	
	@Column(name = "commit_time", insertable = true, updatable = false, nullable = false)
	private LocalDateTime commitTime;
	
	@Column(name = "short_message", length = 128, nullable = false)
	private String shortMessage;
	
	@Column(name = "full_message")
	private String fullMessage;
	
	@Column(name = "create_user_id", insertable = true, updatable = false, nullable = false)
	private Integer createUserId;
	
	@Column(name = "create_time", insertable = true, updatable = false, nullable = false)
	private LocalDateTime createTime;

	public Integer getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Integer repository) {
		this.repositoryId = repository;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public Integer getCommitUserId() {
		return commitUserId;
	}

	public void setCommitUserId(Integer commitUserId) {
		this.commitUserId = commitUserId;
	}

	public LocalDateTime getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(LocalDateTime commitTime) {
		this.commitTime = commitTime;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public String getFullMessage() {
		return fullMessage;
	}

	public void setFullMessage(String fullMessage) {
		this.fullMessage = fullMessage;
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