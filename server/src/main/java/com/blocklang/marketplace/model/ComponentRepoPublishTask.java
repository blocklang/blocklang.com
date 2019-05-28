package com.blocklang.marketplace.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.constant.converter.ReleaseResultConverter;

@Entity
@Table(name = "component_repo_publish_task")
public class ComponentRepoPublishTask extends PartialOperateFields {

	private static final long serialVersionUID = -2695309681346878878L;

	@Column(name = "git_url", nullable = false, unique = true, length = 128)
	private String gitUrl;

	@Column(name = "start_time", nullable = false)
	private LocalDateTime startTime;

	@Column(name = "end_time")
	private LocalDateTime endTime;

	// 因为 publish_result 的值与 release_result 的值相同，所以使用 ReleaseResult 相关类
	@Convert(converter = ReleaseResultConverter.class)
	@Column(name = "publish_result", length = 2, nullable = false)
	private ReleaseResult publishResult;

	@Column(name = "log_file_name", length = 255)
	private String logFileName;

	@Transient
	private String createUserName;
	@Transient
	private String createUserAvatarUrl;

	public String getGitUrl() {
		return gitUrl;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public ReleaseResult getPublishResult() {
		return publishResult;
	}

	public void setPublishResult(ReleaseResult publishResult) {
		this.publishResult = publishResult;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	public String getCreateUserName() {
		return createUserName;
	}

	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}

	public String getCreateUserAvatarUrl() {
		return createUserAvatarUrl;
	}

	public void setCreateUserAvatarUrl(String createUserAvatarUrl) {
		this.createUserAvatarUrl = createUserAvatarUrl;
	}

}
