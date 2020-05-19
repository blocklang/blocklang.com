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

public class TagParser extends AbstractParser {
	
	public TagParser(List<String> tags, MarketplaceStore store, CliLogger logger) {
		super(tags, store, logger);
	}

	// 如果有 tag 解析出错，则不再解析后续的 tag
	public boolean run(String tag) {
		this.version = GitUtils.getVersionFromRefName(tag).orElse(null);
		if(this.version == null) {
			logger.info("从 tag {0} 中未解析出语义版本号，忽略解析此 tag", tag);
			return true;
		}
		
		logger.info("开始解析 v{0}", version);

		// 确认 tag 是否已解析过，如果解析过，则不再解析
		// 如果文件夹存在，则视作已解析过
		Path packageVersionDirectory = store.getPackageVersionDirectory(version);
		if (packageVersionDirectory.toFile().exists()) {
			logger.info("已解析过 v{0}，不再解析", version);
			return true;
		}

		readAllWidgetChangelogs(tag);
		
		// TODO: 在此处校验分组和 widget 的命名规范
		// 格式为 {order}_{description}，或者 {order}__，或者 {order}
		// order 必须为时间戳
		// 并且一个文件夹下不会重复
		// FIXME: 校验同一个文件夹中的时间戳不能重复。

		// 注意，如果遇到 json 文件格式有误或者 schema 校验未通过，则不能退出校验，而是继续校验下一个文件
		// 要校验出所有的错误，便于用于一次修改完成。
		// 校验通过后，应用增量变更
		if (!validateSchema()) {
			success = false;
			return false;
		}
		
		// changelog 文件发布后不允许修改
		if (publishedFileIsUpdate()) {
			success = false;
			return false;
		}
		
		parseAllWidgets(tag);

		// 在文件系统中保存所有的 widget 结构
		if (!success) {
			return false;
		}
		saveAllWidgets();
		return success;
	}
	
	@Override
	protected Widget loadPreviousWidget(String tag, String widgetId) {
		int index = tags.indexOf(tag);

		if (index <= 0) {
			return null;
		}

		String preVersion = GitUtils.getVersionFromRefName(tags.get(index - 1)).get();

		Path widgetPath = store.getPackageVersionDirectory(preVersion).resolve(widgetId).resolve("index.json");
		try {
			String content = Files.readString(widgetPath);
			return JsonUtil.fromJsonObject(content, Widget.class);
		} catch (IOException e) {
			// 如果组件是在该 tag 中新增的，则获取不到上一版本的 idnex.json 属于正常情况
		}
		return null;
	}
	
	@Override
	protected List<PublishedFileInfo> getWidgetPublishedFiles(String widgetId) {
		List<PublishedFileInfo> changeLogs = new ArrayList<>();
		Path widgetChangeLogPath = store.getPackageChangeLogDirectory().resolve(widgetId).resolve("index.json");

		try {
			String widgetChangeLog = Files.readString(widgetChangeLogPath);
			changeLogs.addAll(JsonUtil.fromJsonArray(widgetChangeLog, PublishedFileInfo.class));
		} catch (IOException e1) {
			// 如果文件不存在，则模式使用空 List
		}
		return changeLogs;
	}
}