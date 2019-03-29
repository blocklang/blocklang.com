package com.blocklang.develop.model;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.Assert;

/**
 * 项目级上下文
 * 
 * @author Zhengwei Jin
 *
 */
public class ProjectContext extends AppGlobalContext{
	
	private static final String GIT_REPO_ROOT_PATH = "gitRepo";

	protected String owner;
	protected String projectName;
	protected String description;
	
	public ProjectContext(String projectName, String dataRootPath) {
		super(dataRootPath);
		Assert.hasLength(projectName, "项目名不能为空");
		
		this.projectName = projectName;
		this.dataRootPath = dataRootPath;
	}
	
	public ProjectContext(String owner, String projectName, String projectsRootPath) {
		this(projectName, projectsRootPath);
		Assert.hasLength(owner, "项目拥有者的登录名不能为空");
		
		this.owner = owner;
	}
	
	public ProjectContext(String owner, String projectName, String description, String projectsRootPath) {
		this(owner, projectName, projectsRootPath);
		
		this.description = description;
	}

	public Path getGitRepositoryDirectory() {
		return Paths.get(this.dataRootPath, GIT_REPO_ROOT_PATH, this.owner, this.projectName);
	}

	public String getOwner() {
		return owner;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getDescription() {
		return description;
	}

}
