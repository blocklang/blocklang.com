package com.blocklang.marketplace.apiparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.runner.action.PublishedFileInfo;

public abstract class MasterParser extends AbstractRefParser {

	private static final String MASTER_REF = "refs/heads/master";
	protected static final String MASTER_REF_SHORT_NAME = "master";
	
	public MasterParser(List<String> tags, MarketplaceStore store, CliLogger logger) {
		super(tags, store, logger);
	}

	public ParseResult run() {
		logger.info("开始解析 master 分支");

		readAllChangelogs(MASTER_REF);
		
		if(!hasChangelogs()) {
			logger.info("没有找到 changelog 文件，不解析");
			return ParseResult.ABORT;
		}
		
		if(!validateFileNamePattern()) {
			return ParseResult.FAILED;
		}

		if(!validateJsonSchema()) {
			return ParseResult.FAILED;
		}

		if(publishedFileUpdated()) {
			return ParseResult.FAILED;
		}

		if(!parseAllApi(MASTER_REF)) {
			return ParseResult.FAILED;
		}

		if(!saveAllApi()) {
			return ParseResult.FAILED;
		}

		return ParseResult.SUCCESS;
	}
	
	@Override
	protected <T> T loadPreviousVersion(String fullRefName, String objectId, Class<T> clazz) {
		if(fullRefName.equals(MASTER_REF) && tags.size() > 0) {
			String preVersion = GitUtils.getVersionFromRefName(tags.get(tags.size() - 1)).get();
			Path widgetPath = store.getPackageVersionDirectory(preVersion).resolve(objectId).resolve("index.json");
			try {
				String content = Files.readString(widgetPath);
				return JsonUtil.fromJsonObject(content, clazz);
			} catch (IOException e) {
				// 如果组件是在该 tag 中新增的，则获取不到上一版本的 index.json 属于正常情况
			}
		}
		return null;
	}
	
	@Override
	protected List<PublishedFileInfo> getPublishedFiles(String dirId) {
		List<PublishedFileInfo> changeLogs = new ArrayList<>();
		Path widgetChangeLogPath = store.getPackageChangeLogDirectory().resolve(dirId).resolve("index.json");

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
