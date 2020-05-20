package com.blocklang.marketplace.runner.action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.data.changelog.Widget;

//

/**
 * 解析 master 分支中的 api
 * 
 * <p>不需要判断 master 分支是否解析过，因为每次都要覆盖之前的解析>
 * 
 * @author Zhengwei Jin
 *
 */
public class MasterBranchParser extends AbstractParser{
	
	private String masterRef = "refs/heads/master";
	
	public MasterBranchParser(List<String> tags, MarketplaceStore store, CliLogger logger) {
		super(tags, store, logger);
		super.version = "master";
	}

	public boolean run() {
		logger.info("开始解析 master 分支");
		
		readAllWidgetChangelogs(this.masterRef);
		
		if(!validateSchema()) {
			success = false;
			return false;
		}
		
		if(publishedFileIsUpdate()) {
			success = false;
			return false;
		}
		
		parseAllWidgets(version);
		
		// 在文件系统中保存所有的 widget 结构
		if (!success) {
			return false;
		}
		saveAllWidgets();
		return success;
	}
	
	/**
	 * 获取上一个 tag 中发布的 Widget
	 * 
	 * @param branch      git branch name
	 * @param widgetId widget 标识，取文件名 202005161723_button 中的时间戳
	 * @return 获取上一个 tag 中发布的 Widget，如果不存在上一个 tag，则返回 <code>null</code>
	 */
	@Override
	protected Widget loadPreviousWidget(String branch, String widgetId) {
		if(branch.equals(version) && tags.size() > 0) {
			String preVersion = GitUtils.getVersionFromRefName(tags.get(tags.size() - 1)).get();

			Path widgetPath = store.getPackageVersionDirectory(preVersion).resolve(widgetId).resolve("index.json");
			try {
				String content = Files.readString(widgetPath);
				return JsonUtil.fromJsonObject(content, Widget.class);
			} catch (IOException e) {
				// 如果组件是在该 tag 中新增的，则获取不到上一版本的 idnex.json 属于正常情况
			}
		}
		return null;
	}
	
	@Override
	protected List<PublishedFileInfo> getWidgetPublishedFiles(String widgetId) {
		List<PublishedFileInfo> changeLogs = new ArrayList<>();
		Path widgetChangeLogPath = store.getPackageChangeLogDirectory().resolve(widgetId).resolve("index.json");

		try {
			String widgetChangeLog = Files.readString(widgetChangeLogPath);
			
			List<PublishedFileInfo> published = JsonUtil.fromJsonArray(widgetChangeLog, PublishedFileInfo.class);
			
			// 因为 master 分支每次都要重新解析，所以清除 master 分支的解析记录
			published.removeIf(fileInfo -> fileInfo.getVersion().equals("master"));

			changeLogs.addAll(published);
		} catch (IOException e1) {
			// 如果文件不存在，则模式使用空 List
		}
		return changeLogs;
	}

}
