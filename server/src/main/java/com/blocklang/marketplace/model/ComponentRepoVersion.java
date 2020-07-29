package com.blocklang.marketplace.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.converter.AppTypeConverter;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.converter.LanguageConverter;

@Entity
@Table(name = "component_repo_version", 
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "component_repo_id", "version" })
	}
)
public class ComponentRepoVersion extends PartialIdField {

	private static final long serialVersionUID = 5454786240466551849L;

	@Column(name = "component_repo_id", nullable = false)
	private Integer componentRepoId;
	
	@Column(name = "version", nullable = false, length = 32)
	private String version;
	
	@Column(name = "git_tag_name", nullable = false, length = 32)
	private String gitTagName;
	
	@Column(name = "api_repo_version_id", nullable = false)
	private Integer apiRepoVersionId;

	@Column(name = "name", nullable = false, length = 64)
	private String name;

	@Column(name = "display_name", length = 64)
	private String displayName;

	@Column(name = "description", length = 512)
	private String description;

	@Column(name = "logo_path", length = 64)
	private String logoPath;

	@Convert(converter = LanguageConverter.class)
	@Column(name = "language", length = 32)
	private Language language;
	
	@Column(name = "build", nullable = false, length = 32)
	private String build;
	
	@Column(name = "last_publish_time", nullable = false)
	private LocalDateTime lastPublishTime;

	@Convert(converter = AppTypeConverter.class)
	@Column(name = "app_type", nullable = false, length = 2)
	private AppType appType;

	@Column(name = "create_user_id", insertable = true, updatable = false, nullable = false)
	private Integer createUserId;
	
	@Column(name = "create_time", insertable = true, updatable = false, nullable = false)
	private LocalDateTime createTime;

	public Integer getComponentRepoId() {
		return componentRepoId;
	}

	public void setComponentRepoId(Integer componentRepoId) {
		this.componentRepoId = componentRepoId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Integer getApiRepoVersionId() {
		return apiRepoVersionId;
	}

	public void setApiRepoVersionId(Integer apiRepoVersionId) {
		this.apiRepoVersionId = apiRepoVersionId;
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

	public String getGitTagName() {
		return gitTagName;
	}

	public void setGitTagName(String gitTagName) {
		this.gitTagName = gitTagName;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLogoPath() {
		return logoPath;
	}

	public void setLogoPath(String logoPath) {
		this.logoPath = logoPath;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public LocalDateTime getLastPublishTime() {
		return lastPublishTime;
	}

	public void setLastPublishTime(LocalDateTime lastPublishTime) {
		this.lastPublishTime = lastPublishTime;
	}

	public AppType getAppType() {
		return appType;
	}

	public void setAppType(AppType appType) {
		this.appType = appType;
	}

	public String getBuild() {
		return build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	// FIXME: 注意，此处没有包含 Service
	// Service 算不算是一个 app?
	// 或者是 appType 需要改名？
	public String getIcon() {
		return this.appType.getIcon();
	}
	// FIXME: 注意，此处没有包含 Service
	public String getTitle() {
		return this.appType.getLabel();
	}
}
