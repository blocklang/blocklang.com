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
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.data.changelog.AddWidgetEvent;
import com.blocklang.marketplace.data.changelog.AddWidgetProperty;
import com.blocklang.marketplace.data.changelog.Change;
import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.runner.action.PublishedFileInfo;
import com.blocklang.marketplace.runner.action.WidgetMerger;
import com.blocklang.marketplace.task.CodeGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class WidgetMasterParser extends MasterParser {
	List<Widget> allWidgets = new ArrayList<Widget>();
	private boolean success = true;
	
	public WidgetMasterParser(List<String> tags, MarketplaceStore store, CliLogger logger) {
		super(tags, store, logger);
	}

	@Override
	protected boolean parseAllApi(String fullRefName) {
		CodeGenerator widgetCodeGen = new CodeGenerator(null);
		allGroupedChangelogFiles.forEach((widgetDirectoryName, changelogFiles) -> {
			parseWidget(fullRefName, widgetCodeGen, widgetDirectoryName, changelogFiles);
		});
		return success;
	}
	
	private void parseWidget(String fullRefName, CodeGenerator widgetCodeGen, String widgetDirectoryName,
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
		Widget widget = loadPreviousVersion(fullRefName, widgetId, Widget.class);
		
		boolean anyOperatorsInvalid = false;
		// 开始解析 changelog
		// 注意：要将整个仓库校验一遍。
		// 因为是增量日志，所以只能边应用变更边校验
		// 校验文件中的所有操作，如果有一个操作未通过校验，不中断，仍然继续校验后续的操作
		// 但要利用此标识，不再应用这些变更。
		for (GitBlobInfo jsonFile : changelogFiles) {
			String widgetCode = widgetCodeGen.next();
			String jsonFileId = pathReader.read(jsonFile.getName()).getOrder();
			// 判断该文件是否已执行过
			if (changelogFileParsed(publishedFiles, jsonFileId)) {
				break;
			}
			
			WidgetMerger widgetMerger = new WidgetMerger(allWidgets, widget, logger);
			List<Change> changes = readChangesInOneFile(jsonFile);
			
			if(!widgetMerger.run(changes, widgetCode)) {
				anyOperatorsInvalid = true;
				break; // 一个 Widget 的 changelog 合并出错，则无需再处理后续的合并
			}
			
			widget = widgetMerger.getResult();
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

	private List<Change> readChangesInOneFile(GitBlobInfo jsonFile) {
		// 一次处理一个文件中的变更，而不是将所有所有文件中的变更
		List<Change> changes = new ArrayList<>();
		// 从 json 中获取 changes 列表中的内容，并转换为对应的操作，先实现 createWidget 操作
		try {
			JsonNode jsonNode = JsonUtil.readTree(jsonFile.getContent());
			JsonNode changeNodes = jsonNode.get("changes");
			for (JsonNode operator : changeNodes) {
				// FIXME: 是否可应用策略模式
				if (operator.get("createWidget") != null) {
					Widget widget = JsonUtil.treeToValue(operator.get("createWidget"), Widget.class);
					Change change = new Change("createWidget", widget);
					changes.add(change);
				} else if (operator.get("addProperty") != null) {
					AddWidgetProperty addWidgetProperty = JsonUtil.treeToValue(operator.get("addProperty"),
							AddWidgetProperty.class);
					// 约定 property 按照 code 排序，这样就可以取最后一个 property 的 code

					// 并不需要 widgetName 指定引用关系，因为放在 widget 目录下的操作，都是针对 widget 的
					// 新增的属性列表中，只要有一个属性名被占用，就不能应用变更
					Change change = new Change("addProperty", addWidgetProperty);
					changes.add(change);
				} else if (operator.get("addEvent") != null) {
					AddWidgetEvent addWidgetEvent = JsonUtil.treeToValue(operator.get("addEvent"),
							AddWidgetEvent.class);
					Change change = new Change("addEvent", addWidgetEvent);
					changes.add(change); // TOOD: change.add(op, data);
				}
			}
		} catch (JsonProcessingException e) {
			// do nothing
		}
		return changes;
	}


	@Override
	protected boolean saveAllApi() {
		allWidgets.forEach(widget -> {
			Path widgetPath = store.getPackageVersionDirectory(MASTER_REF_SHORT_NAME).resolve(widget.getId());
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
