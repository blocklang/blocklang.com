package com.blocklang.marketplace.data;

// 不同种类的仓库配置文件定义稍有不同，是为每一种单独定义一个类，还是将所有属性定义到一个类中？
/**
 * 
 * @author Zhengwei Jin
 * @deprecated 使用 RepoConfigJson
 */
public class ApiJson {
	private String name;
	private String displayName;
	@Deprecated
	private String version;
	private String description;
	private String category;
	@Deprecated
	private String[] components;

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

	/**
	 * @deprecated 使用 git tag 中的版本号
	 * @return
	 */
	@Deprecated(since = "v1.0.0")
	public String getVersion() {
		return version;
	}

	/**
	 * @deprecated 使用 git tag 中的版本号
	 * @return
	 */
	@Deprecated(since = "v1.0.0")
	public void setVersion(String version) {
		this.version = version;
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

	/**
	 * @deprecated 直接从 changelog 文件夹中扫描，所以不需要再注册。
	 * @return
	 */
	@Deprecated(since = "v1.0.0")
	public String[] getComponents() {
		if(components == null) {
			return new String[] {};
		}
		return components;
	}

	/**
	 * @deprecated 直接从 changelog 文件夹中扫描，所以不需要再注册。
	 * @return
	 */
	@Deprecated(since = "v1.0.0")
	public void setComponents(String[] components) {
		this.components = components;
	}

}
