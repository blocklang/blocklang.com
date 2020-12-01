package com.blocklang.develop.designer.data;

/**
 * 项目依赖项
 * 
 * @author jinzw
 *
 */
public class Dependency {
	private Integer id;
	private String gitRepoWebsite;
	private String gitRepoOwner;
	private String gitRepoName;
	private Integer apiRepoId;
	private String name;
	private String category;
	private String version;
	private Boolean std;

	/**
	 * 获取 git 仓库标识
	 * @return git 仓库标识
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * 设置 git 仓库标识
	 * @param id git 仓库标识
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * 获取 git 仓库网站
	 * @return git 仓库网站
	 */
	public String getGitRepoWebsite() {
		return gitRepoWebsite;
	}

	/**
	 * 设置 git 仓库网站
	 * @param gitRepoWebsite git 仓库网站
	 */
	public void setGitRepoWebsite(String gitRepoWebsite) {
		this.gitRepoWebsite = gitRepoWebsite;
	}

	/**
	 * 获取 git 仓库拥有者
	 * @return git 仓库拥有者
	 */
	public String getGitRepoOwner() {
		return gitRepoOwner;
	}

	/**
	 * 设置 git 仓库拥有者
	 * @param gitRepoOwner git 仓库拥有者
	 */
	public void setGitRepoOwner(String gitRepoOwner) {
		this.gitRepoOwner = gitRepoOwner;
	}

	/**
	 * 获取 git 仓库名称
	 * @return git 仓库名称
	 */
	public String getGitRepoName() {
		return gitRepoName;
	}

	/**
	 * 设置 git 仓库名称
	 * @param gitRepoName git 仓库名称
	 */
	public void setGitRepoName(String gitRepoName) {
		this.gitRepoName = gitRepoName;
	}

	/**
	 * 获取该组件仓库实现的 API 仓库标识
	 * @return 该组件仓库实现的 API 仓库标识
	 */
	public Integer getApiRepoId() {
		return apiRepoId;
	}

	/**
	 * 设置该组件仓库实现的 API 仓库标识
	 * @param apiRepoId 该组件仓库实现的 API 仓库标识
	 */
	public void setApiRepoId(Integer apiRepoId) {
		this.apiRepoId = apiRepoId;
	}

	/**
	 * 获取组件库的名称
	 * @return 组件库的名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置组件库的名称
	 * @param name 组件库的名称
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取组件库分类
	 * @return 组件库分类
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * 设置组件库分类
	 * @param category 组件库分类
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * 获取版本号
	 * @return 版本号
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * 设置版本号
	 * @param version 版本号
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * 确认是否标准库
	 * @return 是否标准库
	 */
	public Boolean isStd() {
		return std;
	}

	/**
	 * 设置是否标准库
	 * @param std 是否标准库
	 */
	public void setStd(Boolean std) {
		this.std = std;
	}

}
