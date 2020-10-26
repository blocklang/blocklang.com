package com.blocklang.develop.model;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.Assert;

/**
 * 仓库级上下文
 * 
 * @author Zhengwei Jin
 *
 */
public class RepositoryContext extends AppGlobalContext{
	
	// 项目的模型信息都存在 models 目录下。
	private static final String GIT_REPO_ROOT_PATH = "models";

	private Integer repositoryId;
	protected String owner;
	protected String repoName;
	protected String description;
	
	protected RepositoryContext() { }
	
	public RepositoryContext(String repoName, String dataRootPath) {
		super(dataRootPath);
		Assert.hasLength(repoName, "仓库名不能为空");
		
		this.repoName = repoName;
		this.dataRootPath = dataRootPath;
	}
	
	public RepositoryContext(String owner, String repoName, String dataRootPath) {
		this(repoName, dataRootPath);
		Assert.hasLength(owner, "项目拥有者的登录名不能为空");
		
		this.owner = owner;
	}
	
	public RepositoryContext(String owner, String repoName, String description, String dataRootPath) {
		this(owner, repoName, dataRootPath);
		
		this.description = description;
	}

	public Path getGitRepositoryDirectory() {
		return Paths.get(this.dataRootPath, GIT_REPO_ROOT_PATH, this.owner, this.repoName);
	}

	public Integer getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId) {
		this.repositoryId = repositoryId;
	}

	protected void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}
	
	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getDescription() {
		return description;
	}

}
