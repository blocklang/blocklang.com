package com.blocklang.marketplace.data;

/**
 * 
 *  @deprecated 统一使用一个配置文件类 RepoConfigJson
 */
public class ComponentJson extends ApiJson{

	private String language;
	private String icon;
	private String baseOn; // 不再使用，改为 build
	private Api api;
	private String appType;
	private Boolean dev = false; // 对应实体对象中的 isIdeExtension
	private Boolean std = false; // 是否属于标准库

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Api getApi() {
		return api;
	}

	public void setApi(Api api) {
		this.api = api;
	}

	public Boolean isDev() {
		return dev;
	}

	public void setDev(Boolean dev) {
		this.dev = dev;
	}

	public String getBaseOn() {
		return baseOn;
	}

	public void setBaseOn(String baseOn) {
		this.baseOn = baseOn;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public Boolean isStd() {
		return std;
	}

	public void setStd(Boolean std) {
		this.std = std;
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
