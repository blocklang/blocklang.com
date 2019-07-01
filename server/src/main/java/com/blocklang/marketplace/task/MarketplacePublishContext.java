package com.blocklang.marketplace.task;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.blocklang.marketplace.data.ApiJson;
import com.blocklang.marketplace.data.ComponentJson;
import com.blocklang.marketplace.data.LocalRepoPath;
import com.blocklang.marketplace.data.changelog.ComponentChangeLogs;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;

/**
 * 在这里统一存储和查询发布过程中的信息
 * 
 * @author Zhengwei Jin
 *
 */
public class MarketplacePublishContext {
	
	private ComponentRepoPublishTask publishTask;
	private boolean isFirstPublish = true;
	private String dataRootPath;
	
	private LocalRepoPath localComponentRepoPath;
	private LocalRepoPath localApiRepoPath;
	private Path logFile;
	
	private String componentRepoLatestVersion;
	
	// tag name 是包含 ref/tags/ 的完整名
	private String apiRepoTagName;
	
	// 组件库描述信息
	// 组件库最新版本中 component.json 中的内容
	private ComponentJson componentJson;
	private ApiJson apiJson;
	
	// 按组件分组，并按照版本号正序排列的 changelog
	private List<ComponentChangeLogs> changeLogs;
	
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

	public List<ComponentChangeLogs> getChangeLogs() {
		return changeLogs;
	}

	public void setChangeLogs(List<ComponentChangeLogs> changeLogs) {
		this.changeLogs = changeLogs;
	}

	public String getComponentRepoLatestVersion() {
		return componentRepoLatestVersion;
	}

	public void setComponentRepoLatestVersion(String componentRepoLatestVersion) {
		this.componentRepoLatestVersion = componentRepoLatestVersion;
	}

	public ComponentRepoPublishTask getPublishTask() {
		return publishTask;
	}

	public void setPublishTask(ComponentRepoPublishTask publishTask) {
		this.publishTask = publishTask;
	}

	public boolean isFirstPublish() {
		return isFirstPublish;
	}

	public void setFirstPublish(boolean isFirstPublish) {
		this.isFirstPublish = isFirstPublish;
	}

	public String getApiRepoTagName() {
		return apiRepoTagName;
	}

	public void setApiRepoTagName(String apiRepoTagName) {
		this.apiRepoTagName = apiRepoTagName;
	}

}
