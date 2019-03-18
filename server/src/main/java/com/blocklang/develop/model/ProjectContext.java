package com.blocklang.develop.model;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.Assert;

public class ProjectContext {
	
	private static final String GIT_REPO_ROOT_PATH = "gitRepo";

	protected String owner;
	protected String projectName;
	protected String description;
	public String projectsRootPath; // block lang 站点的项目文件根目录
	
	public ProjectContext(String projectName, String projectsRootPath) {
		Assert.hasLength(projectName, "项目名不能为空");
		Assert.hasLength(projectsRootPath, "存放项目的根路径不能为空");
		
		this.projectName = projectName;
		this.projectsRootPath = projectsRootPath;
	}
	
	public ProjectContext(String owner, String projectName, String projectsRootPath) {
		Assert.hasLength(owner, "项目拥有者的登录名不能为空");
		Assert.hasLength(projectName, "项目名不能为空");
		Assert.hasLength(projectsRootPath, "存放项目的根路径不能为空");
		
		this.owner = owner;
		this.projectName = projectName;
		this.projectsRootPath = projectsRootPath;
	}
	
	public ProjectContext(String owner, String projectName, String description, String projectsRootPath) {
		Assert.hasLength(owner, "项目拥有者的登录名不能为空");
		Assert.hasLength(projectName, "项目名不能为空");
		Assert.hasLength(projectsRootPath, "存放项目的根路径不能为空");
		
		this.owner = owner;
		this.projectName = projectName;
		this.description = description;
		this.projectsRootPath = projectsRootPath;
	}

	public Path getGitRepositoryDirectory() {
		return Paths.get(this.projectsRootPath, GIT_REPO_ROOT_PATH, this.owner, this.projectName);
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
