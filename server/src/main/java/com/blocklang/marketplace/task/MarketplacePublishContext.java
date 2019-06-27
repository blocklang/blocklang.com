package com.blocklang.marketplace.task;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.blocklang.marketplace.data.ApiJson;
import com.blocklang.marketplace.data.ComponentJson;
import com.blocklang.marketplace.data.LocalRepoPath;
import com.blocklang.marketplace.data.changelog.ComponentChangeLog;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

/**
 * 在这里统一存储和查询发布过程中的信息
 * 
 * @author Zhengwei Jin
 *
 */
public class MarketplacePublishContext {
	
	private String dataRootPath;
	
	private LocalRepoPath localComponentRepoPath;
	private LocalRepoPath localApiRepoPath;
	private Path logFile;
	
	private String componentRepoLatestVersion;
	
	// 组件库描述信息
	// 组件库最新版本中 component.json 中的内容
	private ComponentJson componentJson;
	private ApiJson apiJson;
	
	// 组件库基本信息
	private ComponentRepo componentRepo;
	// 组件库的最新版本
	private ComponentRepoVersion componentRepoVersion;
	
	// API 库基本信息
	private ApiRepo apiRepo;
	// API 库的所有版本信息
	private List<ApiRepoVersion> apiRepoVersions;
	
	// 按组件分组，并按照版本号正序排列的 changelog
	// 只包含未发布的 changelog？
	// 如何获取上一个版本？
	private List<ComponentChangeLog> changeLogs;
	
	
	public MarketplacePublishContext(String dataRootPath, String componentGitUrl) {
		this.dataRootPath = dataRootPath;
		this.localComponentRepoPath = new LocalRepoPath(dataRootPath, componentGitUrl);
	}
	
	public void parseApiGitUrl(String apiGitUrl) {
		this.localApiRepoPath = new LocalRepoPath(dataRootPath, apiGitUrl);
	}

	public Path getRepoPublishLogFile() {
		if(this.logFile == null) {
			this.logFile = this.getRepoPublishLogDirectory().resolve(this.getRepoPublishLogFileName());
		}
		return this.logFile;
	}

	private String getRepoPublishLogFileName() {
		LocalDateTime startLogTime = LocalDateTime.now();
		return startLogTime.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + ".log";
	}

	private Path getRepoPublishLogDirectory() {
		return this.localComponentRepoPath.getRepoRootDirectory().resolve("publishLogs");
	}
	
	public LocalRepoPath getLocalComponentRepoPath() {
		return localComponentRepoPath;
	}

	public LocalRepoPath getLocalApiRepoPath() {
		return localApiRepoPath;
	}

	public ComponentJson getComponentJson() {
		return componentJson;
	}

	public void setComponentJson(ComponentJson componentJson) {
		this.componentJson = componentJson;
	}

	public ApiJson getApiJson() {
		return apiJson;
	}

	public void setApiJson(ApiJson apiJson) {
		this.apiJson = apiJson;
	}

	public ComponentRepo getComponentRepo() {
		return componentRepo;
	}

	public void setComponentRepo(ComponentRepo componentRepo) {
		this.componentRepo = componentRepo;
	}

	public ComponentRepoVersion getComponentRepoVersion() {
		return componentRepoVersion;
	}

	public void setComponentRepoVersion(ComponentRepoVersion componentRepoVersion) {
		this.componentRepoVersion = componentRepoVersion;
	}

	public ApiRepo getApiRepo() {
		return apiRepo;
	}

	public void setApiRepo(ApiRepo apiRepo) {
		this.apiRepo = apiRepo;
	}

	public List<ApiRepoVersion> getApiRepoVersions() {
		return apiRepoVersions;
	}

	public void setApiRepoVersions(List<ApiRepoVersion> apiRepoVersions) {
		this.apiRepoVersions = apiRepoVersions;
	}

	public List<ComponentChangeLog> getChangeLogs() {
		return changeLogs;
	}

	public void setChangeLogs(List<ComponentChangeLog> changeLogs) {
		this.changeLogs = changeLogs;
	}

	public String getComponentRepoLatestVersion() {
		return componentRepoLatestVersion;
	}

	public void setComponentRepoLatestVersion(String componentRepoLatestVersion) {
		this.componentRepoLatestVersion = componentRepoLatestVersion;
	}

}
