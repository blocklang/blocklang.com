package com.blocklang.marketplace.apiparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.blocklang.core.git.GitBlobInfo;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.apiparser.widget.WidgetData;
import com.blocklang.marketplace.apiparser.widget.WidgetOperator;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.runner.action.PublishedFileInfo;

public class WidgetMasterParser extends MasterParser {
	List<WidgetData> allWidgets = new ArrayList<WidgetData>();
	private boolean success = true;
	
	public WidgetMasterParser(List<String> tags, MarketplaceStore store, CliLogger logger) {
		super(tags, store, logger);
	}

	@Override
	protected boolean parseAllApi(String fullRefName) {
		allGroupedChangelogFiles.forEach((widgetDirectoryName, changelogFiles) -> {
			parseWidget(fullRefName, widgetDirectoryName, changelogFiles);
		});
		return success;
	}
	
	private void parseWidget(String fullRefName, String widgetDirectoryName,
			List<GitBlobInfo> changelogFiles) {
		String widgetId = pathReader.read(widgetDirectoryName).getOrder();
		// 在上一个版本的基础上增量安装
		// 首次安装

		// changes 中会包含多个变更操作，所以不仅仅是在版本之间，在一个变更文件的多个变更操作之间也需要增量操作
		// 如果是第一个分支，则不需要往前追溯
		// 要在每一个 tag 中都记录哪些 changelog 已执行过
		// 或者在一个文件中记录所有执行过的 changelog，并与 tag 关联起来
		// 如使用如下 json 数组记录，并存在 package/__changelog__/{widget}/changlog.json
		// [{version: "0.1.0", fileId: "202005161800", md5sum: "xxxdswewe"}]
		List<PublishedFileInfo> publishedFiles = getPublishedFiles(widgetId);
		// 应先将所有文件层面的有效性校验完之后再处理
		// 开始增量应用变更操作，每一步变更都要输出结果，先从零开始。
		// 需要定义实体类
		// 如果变更文件已执行过，则不再执行
		// 校验变更文件的内容是否已修改，如果修改，则给出提示
		WidgetData widget = loadPreviousVersion(fullRefName, widgetId, WidgetData.class);
		if(widget != null) {
			operatorContext.addWidget(widget);
		}
		
		boolean anyOperatorsInvalid = false;
		// 开始解析 changelog
		// 注意：要将整个仓库校验一遍。
		// 因为是增量日志，所以只能边应用变更边校验
		// 校验文件中的所有操作，如果有一个操作未通过校验，不中断，仍然继续校验后续的操作
		// 但要利用此标识，不再应用这些变更。
		for (GitBlobInfo jsonFile : changelogFiles) {
			String jsonFileId = pathReader.read(jsonFile.getName()).getOrder();
			// 判断该文件是否已执行过
			if (changelogFileParsed(publishedFiles, jsonFileId)) {
				break;
			}
			
			List<WidgetOperator> changes = readChangesInOneFile(jsonFile);
			for (WidgetOperator change : changes) {
				anyOperatorsInvalid = !change.apply(operatorContext);
			}
			
			if(anyOperatorsInvalid) {
				break; // 一个 Widget 的 changelog 合并出错，则无需再处理后续的合并
			}
			
			widget = operatorContext.getSelectedWidget();
			widget.setId(widgetId);
			
			// 执行完成后，在 changelog 中追加记录
			PublishedFileInfo changeLog = new PublishedFileInfo();
			changeLog.setFileId(jsonFileId);
			changeLog.setVersion(MASTER_REF_SHORT_NAME);
			changeLog.setMd5sum(DigestUtils.md5Hex(jsonFile.getContent()));
			publishedFiles.add(changeLog);
		}

		if (anyOperatorsInvalid) {
			success = false;
			return;
		}

		// 更新 changelog 文件
		// TODO: 完善逻辑，前面已经出错了，是否还需要执行后面的操作。
		// 该功能实现完之后，如何找到合理的关节来重构呢？

		// 往文件系统保存 result
		// 将一个 widget 上的变更都应用之后再保存
		if (widget != null) {
			allWidgets.add(widget);
			allPublishedFiles.put(widgetId, publishedFiles);
		}
	}
	
	private boolean changelogFileParsed(List<PublishedFileInfo> changelogs, String fileId) {
		return changelogs.stream().anyMatch(changeLog -> changeLog.getFileId().equals(fileId));
	}

	@Override
	protected boolean saveAllApi(String shortRefName) {
		allWidgets.forEach(widget -> {
			Path widgetPath = store.getPackageVersionDirectory(shortRefName).resolve(widget.getId());
			try {
				Files.createDirectories(widgetPath);
				Files.writeString(widgetPath.resolve("index.json"), JsonUtil.stringify(widget));
			} catch (IOException e) {
				logger.error(e);
				success = false;
			}
		});
		allPublishedFiles.forEach((widgetId, changelogs) -> {
			Path widgetChangelogDirectory = store.getPackageChangeLogDirectory().resolve(widgetId);
			try {
				Files.createDirectories(widgetChangelogDirectory);
				Files.writeString(widgetChangelogDirectory.resolve("index.json"), JsonUtil.stringify(changelogs));
			} catch (IOException e) {
				logger.error(e);
				success = false;
			}
		});
		return success;
	}
}
