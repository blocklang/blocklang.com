package com.blocklang.marketplace.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.core.util.GitUrlParser;
import com.blocklang.core.util.GitUrlSegment;
import com.blocklang.marketplace.constant.PublishType;
import com.blocklang.marketplace.constant.converter.PublishTypeConverter;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.constant.converter.ReleaseResultConverter;

/**
 * 发布组件库的任务信息
 * 
 * @author Zhengwei Jin
 *
 */
@Entity
@Table(name = "component_repo_publish_task", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "git_url", "seq", "create_user_id" })
	}
)
public class ComponentRepoPublishTask extends PartialOperateFields {

	private static final long serialVersionUID = -2695309681346878878L;

	@Column(name = "git_url", nullable = false, length = 128)
	private String gitUrl;
	
	@Column(name = "seq", nullable = false)
	private Integer seq;

	@Column(name = "start_time", nullable = false)
	private LocalDateTime startTime;

	@Column(name = "end_time")
	private LocalDateTime endTime;
	
	@Convert(converter = PublishTypeConverter.class)
	@Column(name = "publish_type", length = 2, nullable = false)
	private PublishType publishType = PublishType.NEW;

	// 因为 publish_result 的值与 release_result 的值相同，所以使用 ReleaseResult 相关类
	@Convert(converter = ReleaseResultConverter.class)
	@Column(name = "publish_result", length = 2, nullable = false)
	private ReleaseResult publishResult = ReleaseResult.INITED;

	@Column(name = "log_file_name", length = 255)
	private String logFileName;
	
	@Column(name = "from_version", length = 32)
	private String fromVersion;
	
	@Column(name = "to_version", length = 32)
	private String toVersion;

	@Transient
	private String createUserName;
	@Transient
	private String createUserAvatarUrl;
	@Transient
	private GitUrlSegment gitUrlSegment;

	public String getGitUrl() {
		return gitUrl;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
		this.gitUrlSegment = GitUrlParser.parse(gitUrl).orElse(null);
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

	public PublishType getPublishType() {
		return publishType;
	}

	public void setPublishType(PublishType publishType) {
		this.publishType = publishType;
	}

	public String getFromVersion() {
		return fromVersion;
	}

	public void setFromVersion(String fromVersion) {
		this.fromVersion = fromVersion;
	}

	public String getToVersion() {
		return toVersion;
	}

	public void setToVersion(String toVersion) {
		this.toVersion = toVersion;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}


	public String getWebsite() {
		return this.gitUrlSegment.getWebsite();
	}

	public String getOwner() {
		return this.gitUrlSegment.getOwner();
	}

	public String getRepoName() {
		return this.gitUrlSegment.getRepoName();
	}
}
