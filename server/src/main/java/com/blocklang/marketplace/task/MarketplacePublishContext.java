package com.blocklang.marketplace.task;

import java.nio.file.Path;

public class MarketplacePublishContext {
	
	private String dataRootPath; // block lang 站点的项目文件根目录
	private String website;
	protected String owner;
	protected String projectName;
	
	public MarketplacePublishContext(String dataRootPath, String website, String owner, String projectName) {
		this.dataRootPath = dataRootPath;
		this.website = website;
		this.owner = owner;
		this.projectName = projectName;
	}

	public Path getRepoSourceDirectory() {
		return this.getRepoDirectory().resolve("source");
	}

	public Path getRepoPublishLogDirectory() {
		return this.getRepoDirectory().resolve("publishLogs");
	}
	
	private Path getRepoDirectory() {
		return Path.of(this.dataRootPath, "marketplace", this.website, this.owner, this.projectName);
	}
	
}
