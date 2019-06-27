package com.blocklang.marketplace.task;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.blocklang.marketplace.data.ComponentJson;
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
	
	// 组件库描述信息
	private ComponentJson componentJson;
	//private ApiJson apiJson;
	
	
	// 组件库最新版本中 component.json 中的内容
	private ComponentRepo componentRepo;
	// 组件库的最新版本
	private ComponentRepoVersion componentRepoVersion;
	
	
	
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

	
	
	

}
