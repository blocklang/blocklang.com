package com.blocklang.marketplace.task;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MarketplacePublishContext{
	
	private String dataRootPath;
	
	private LocalRepoInfo componentRepo;
	private LocalRepoInfo apiRepo;
	private Path logFile;
	
	public MarketplacePublishContext(String dataRootPath, String componentGitUrl) {
		this.dataRootPath = dataRootPath;
		this.componentRepo = new LocalRepoInfo(dataRootPath, componentGitUrl);
	}
	
	public void parseApiGitUrl(String apiGitUrl) {
		this.apiRepo = new LocalRepoInfo(dataRootPath, apiGitUrl);
	}

	public Path getRepoPublishLogFile() {
		if(this.logFile == null) {
			this.logFile = this.getRepoPublishLogDirectory().resolve(this.getRepoPublishLogFileName());
		}
		return this.logFile;
	}

	public LocalRepoInfo getComponentRepo() {
		return componentRepo;
	}

	public LocalRepoInfo getApiRepo() {
		return apiRepo;
	}

	private String getRepoPublishLogFileName() {
		LocalDateTime startLogTime = LocalDateTime.now();
		return startLogTime.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + ".log";
	}

	private Path getRepoPublishLogDirectory() {
		return this.componentRepo.getRepoRootDirectory().resolve("publishLogs");
	}
}
