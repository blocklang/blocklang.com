package com.blocklang.develop.model;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.Assert;

/**
 * 全局上下文
 * 
 * @author Zhengwei Jin
 *
 */
public class AppGlobalContext {
	
	private static final String APPS_DIRECTORY_NAME = "apps";
	private static final String REPO_DIRECTORY_NAME = "repository";

	protected String dataRootPath; // block lang 站点的项目文件根目录
	protected String mavenRootPath;
	
	protected AppGlobalContext() { }
	
	public AppGlobalContext(String dataRootPath) {
		Assert.hasLength(dataRootPath, "存放项目数据的根路径不能为空");
		
		this.dataRootPath = dataRootPath;
	}
	
	public AppGlobalContext(String dataRootPath, String mavenRootPath) {
		this(dataRootPath);
		
		Assert.hasLength(mavenRootPath, "maven 仓库的根路径不能为空");
		this.mavenRootPath = mavenRootPath;
	}

	protected void setDataRootPath(String dataRootPath) {
		this.dataRootPath = dataRootPath;
	}

	public String getDataRootPath() {
		return this.dataRootPath;
	}
	
	public String getMavenRootPath() {
		return this.mavenRootPath;
	}

	// 不要存储绝对路径，不便于迁移
	public Path getAppsDirectory() {
		return Paths.get(this.dataRootPath, APPS_DIRECTORY_NAME);
	}
	
	public Path getMavenRepositoryRootDirectory() {
		return Paths.get(this.mavenRootPath, REPO_DIRECTORY_NAME);
	}
	
}
