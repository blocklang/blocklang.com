package com.blocklang.marketplace.data.changelog;

import java.util.List;

/**
 * 一个组件的所有变更
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
	private String latestPublishVersion;
	
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
	
}
