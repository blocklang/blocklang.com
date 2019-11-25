package com.blocklang.marketplace.data.changelog;

import java.util.List;

import de.skuzzle.semantic.Version;

/**
 * 一个组件库的所有变更
 * 
 * <ol>
 * <li>按组件分组
 * <li>按版本正序拍罗列
 * </ol>
 * 
 * 
 * @author Zhengwei Jin
 *
 */
public class ComponentChangeLogs {

	//组件名中包含相对 API 根目录的相对路径
	private String componentName;
	private List<ChangeLog> changeLogs;
	// 最近一次发布的版本号，每次都是基于上一个版本发布的
	// 每个部件的版本号可能会不相同
	private String latestPublishVersion;
	private String newlyVersion; // 部件的最新版本，可能已发布，也可能未发布
	
	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public List<ChangeLog> getChangeLogs() {
		return changeLogs;
	}

	public void setChangeLogs(List<ChangeLog> changeLogs) {
		this.changeLogs = changeLogs;
	}

	public String getLatestPublishVersion() {
		return latestPublishVersion;
	}

	public void setLatestPublishVersion(String latestPublishVersion) {
		this.latestPublishVersion = latestPublishVersion;
	}

	public String getNewlyVersion() {
		return newlyVersion;
	}

	public void setNewlyVersion(String newlyVersion) {
		this.newlyVersion = newlyVersion;
	}

	public boolean isFirstSetup() {
		return this.latestPublishVersion == null;
	}

	public boolean hasNewVersion() {
		if(this.latestPublishVersion == null) {
			if(this.newlyVersion == null) {
				return false;
			}else {
				return true;
			}
		}
		return Version.parseVersion(this.newlyVersion).isGreaterThan(Version.parseVersion(this.latestPublishVersion));
	}
	
}
