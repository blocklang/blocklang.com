package com.blocklang.marketplace.data;

import java.util.List;

public class RepoConfigJson {
	private String repo;
	private String name;
	private String displayName;
	private String description;
	private String category;
	private String appType;
	private String build;
	private boolean std;
	private List<String> components;
	private Api api;

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getBuild() {
		return build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public boolean isStd() {
		return std;
	}

	public void setStd(boolean std) {
		this.std = std;
	}

	public List<String> getComponents() {
		return components;
	}

	public void setComponents(List<String> components) {
		this.components = components;
	}

	public Api getApi() {
		return api;
	}

	public void setApi(Api api) {
		this.api = api;
	}

	public class Api {
		private String git;
		private String version;

		public String getGit() {
			return git;
		}

		public void setGit(String git) {
			this.git = git;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}
	}
}
