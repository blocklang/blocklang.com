package com.blocklang.marketplace.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.converter.RepoCategoryConverter;

@Entity
@Table(name = "api_repo", uniqueConstraints = { @UniqueConstraint(columnNames = { "create_user_id", "git_repo_url" }) })
public class ApiRepo extends PartialOperateFields {

	private static final long serialVersionUID = -7380348480679301101L;

	@Column(name = "git_repo_url", nullable = false, length = 128)
	private String gitRepoUrl;

	@Column(name = "git_repo_website", nullable = false, length = 32)
	private String gitRepoWebsite;

	@Column(name = "git_repo_owner", nullable = false, length = 64)
	private String gitRepoOwner;

	@Column(name = "git_repo_name", nullable = false, length = 64)
	private String gitRepoName;

	@Convert(converter = RepoCategoryConverter.class)
	@Column(name = "category", nullable = false, length = 2)
	private RepoCategory category;

	public String getGitRepoUrl() {
		return gitRepoUrl;
	}

	public void setGitRepoUrl(String gitRepoUrl) {
		this.gitRepoUrl = gitRepoUrl;
	}

	public String getGitRepoWebsite() {
		return gitRepoWebsite;
	}

	public void setGitRepoWebsite(String gitRepoWebsite) {
		this.gitRepoWebsite = gitRepoWebsite;
	}

	public String getGitRepoOwner() {
		return gitRepoOwner;
	}

	public void setGitRepoOwner(String gitRepoOwner) {
		this.gitRepoOwner = gitRepoOwner;
	}

	public String getGitRepoName() {
		return gitRepoName;
	}

	public void setGitRepoName(String gitRepoName) {
		this.gitRepoName = gitRepoName;
	}

	public RepoCategory getCategory() {
		return category;
	}

	public void setCategory(RepoCategory category) {
		this.category = category;
	}

}
