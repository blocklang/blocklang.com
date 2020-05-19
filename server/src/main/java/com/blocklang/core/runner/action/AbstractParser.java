package com.blocklang.core.runner.action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

import com.blocklang.core.git.GitBlobInfo;
import com.blocklang.core.git.GitFileInfo;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.data.changelog.AddWidgetEvent;
import com.blocklang.marketplace.data.changelog.AddWidgetProperty;
import com.blocklang.marketplace.data.changelog.Change;
import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.schema.ApiChangeSetValidator;
import com.blocklang.marketplace.task.CodeGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

public abstract class AbstractParser {
	protected List<String> tags;
	protected MarketplaceStore store;
	protected String version;
	protected CliLogger logger;
	protected boolean success = true;
	
	List<Widget> allWidgets = new ArrayList<Widget>();
	Map<String, List<PublishedFileInfo>> allPublishedFiles = new HashMap<String, List<PublishedFileInfo>>();
	LinkedHashMap<String, List<GitBlobInfo>> allWidgetChangelogFiles;
	
	public AbstractParser(List<String> tags, MarketplaceStore store, CliLogger logger) {
		this.tags = tags;
		this.store = store;
		this.logger = logger;
	}

	protected void saveAllWidgets() {
		allWidgets.forEach(widget -> {
			Path widgetPath = store.getPackageVersionDirectory(version).resolve(widget.getId());
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
	}

	protected void parseAllWidgets(String branchName) {
		CodeGenerator widgetCodeGen = new CodeGenerator(null);
		allWidgetChangelogFiles.forEach((widgetDirectoryName, changelogFiles) -> {
			parseWidget(branchName, widgetCodeGen, widgetDirectoryName, changelogFiles);
		});
	}
	
	private void parseWidget(String tag, CodeGenerator widgetCodeGen, String widgetDirectoryName,
			List<GitBlobInfo> changelogFiles) {
		String widgetId = widgetDirectoryName.split("__")[0];
		// 在上一个版本的基础上增量安装
		// 首次安装

		// changes 中会包含多个变更操作，所以不仅仅是在版本之间，在一个变更文件的多个变更操作之间也需要增量操作
		// 如果是第一个分支，则不需要往前追溯
		// 要在每一个 tag 中都记录哪些 changelog 已执行过
		// 或者在一个文件中记录所有执行过的 changelog，并与 tag 关联起来
		// 如使用如下 json 数组记录，并存在 package/__changelog__/{widget}/changlog.json
		// [{version: "0.1.0", fileId: "202005161800", md5sum: "xxxdswewe"}]
		
		List<PublishedFileInfo> publishedFiles = getWidgetPublishedFiles(widgetId);

		// 应先将所有文件层面的有效性校验完之后再处理

		// 开始增量应用变更操作，每一步变更都要输出结果，先从零开始。
		// 需要定义实体类
		// 如果变更文件已执行过，则不再执行
		// 校验变更文件的内容是否已修改，如果修改，则给出提示
		Widget widget = loadPreviousWidget(tag, widgetId);
		
		boolean anyOperatorsInvalid = false;
		// 开始解析 changelog
		for (GitBlobInfo jsonFile : changelogFiles) {
			String widgetCode = widgetCodeGen.next();
			String jsonFileId = jsonFile.getName().split("__")[0];
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
			changeLog.setVersion(version);
			changeLog.setMd5sum(DigestUtils.md5Hex(jsonFile.getContent()));
			publishedFiles.add(changeLog);
			

			// 注意：要将整个仓库校验一遍。
			// 因为是增量日志，所以只能边应用变更边校验

			// 校验文件中的所有操作，如果有一个操作未通过校验，不中断，仍然继续校验后续的操作
			// 但要利用此标识，不再应用这些变更。
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
	
	private List<Change> readChangesInOneFile(GitBlobInfo jsonFile) {
		// 一次处理一个文件中的变更，而不是将所有所有文件中的变更
		List<Change> changes = new ArrayList<>();
		// 从 json 中获取 changes 列表中的内容，并转换为对应的操作，先实现 createWidget 操作
		try {
			JsonNode jsonNode = JsonUtil.readTree(jsonFile.getContent());
			JsonNode changeNodes = jsonNode.get("changes");
			for (JsonNode operator : changeNodes) {
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


	/**
	 * 获取上一个 tag 中发布的 Widget
	 * 
	 * @param tag      git tag name
	 * @param widgetId widget 标识，取文件名 202005161723_button 中的时间戳
	 * @return 获取上一个 tag 中发布的 Widget，如果不存在上一个 tag，则返回 <code>null</code>
	 */
	protected abstract Widget loadPreviousWidget(String tag, String widgetId);

	private boolean changelogFileParsed(List<PublishedFileInfo> changelogs, String fileId) {
		return changelogs.stream().anyMatch(changeLog -> changeLog.getFileId().equals(fileId));
	}
	
	protected boolean publishedFileIsUpdate() {
		boolean hasPublishedChangeLogUpdated = false;
		
		for(Map.Entry<String, List<GitBlobInfo>> entry : allWidgetChangelogFiles.entrySet()) {
			String widgetDirectoryName = entry.getKey();
			List<GitBlobInfo> changelogFiles = entry.getValue();
			
			String widgetId = widgetDirectoryName.split("__")[0];
			List<PublishedFileInfo> changeLogs = getWidgetPublishedFiles(widgetId);

			// 应先将所有文件层面的有效性校验完之后再处理

			// 校验已发布的文件是否被修改过
			
			for (GitBlobInfo jsonFile : changelogFiles) {
				String jsonFileId = jsonFile.getName().split("__")[0];
				// 判断该文件是否已执行过
				Optional<PublishedFileInfo> changeLogInfoOption = changeLogs.stream()
						.filter(changeLog -> changeLog.getFileId().equals(jsonFileId)).findFirst();

				if (changeLogInfoOption.isPresent()) {
					// 如果已执行过，则跳过

					// 如果已执行过，但文件内容已修改，则给出错误提示
					String md5sumPublished = changeLogInfoOption.get().getMd5sum();
					String md5sumNow = DigestUtils.md5Hex(jsonFile.getContent());
					if (!md5sumPublished.equals(md5sumNow)) {
						logger.error("{0}/{1} 已被修改，已应用版本的 checksum 为 {2}，但当前版本的 checksum 为 {3}", widgetDirectoryName,
								jsonFile.getName(), md5sumPublished, md5sumNow);
						hasPublishedChangeLogUpdated = true;
					}
				}
			}
		}
		return hasPublishedChangeLogUpdated;
	}

	protected abstract List<PublishedFileInfo> getWidgetPublishedFiles(String widgetId);
	
	// 不是遇见错误就退出，而是所有文件都要校验一遍
	protected boolean validateSchema() {
		boolean allValid = true;
		
		for(Map.Entry<String, List<GitBlobInfo>> entry : allWidgetChangelogFiles.entrySet()) {
			// 目录名由两部分组成，如 `202005151827_button`，前半部分是时间戳，是不允许改变的，后半部分是 widget 名，是可以重命名的
			// 确保 jsonFiles 是按时间戳先后排序的
			List<GitBlobInfo> changelogFiles = entry.getValue();
			changelogFiles.sort(Comparator.comparing(GitFileInfo::getName));
			for(GitBlobInfo fileInfo : changelogFiles) {
				// 校验文件
				JsonNode jsonContent = null;
				try {
					jsonContent = JsonUtil.readTree(fileInfo.getContent());
				} catch (JsonProcessingException e) {
					allValid = false;
					logger.error("{0} 文件中的 json 无效", fileInfo.getName());
					logger.error(e);
				}
				
				if (jsonContent != null) {
					Set<ValidationMessage> errors = ApiChangeSetValidator.run(jsonContent);
					if (!errors.isEmpty()) {
						allValid = false;
						errors.forEach(error -> logger.error(error.getMessage()));
					}
				}
			}
			
		}
		return allValid;
	}
	
	protected void readAllWidgetChangelogs(String tag) {
		// 不 checkout，直接从 git 仓库中读取 tag 下的所有 json 文件
		List<GitFileInfo> allJsonFile = GitUtils
				.readAllFiles(store.getRepoSourceDirectory(), tag, ".json")
				.stream()
				// 将没有存在 changelog 中的 json 文件排除掉
				// 约定所有变更都存在项目根目录下的 changelog 文件夹中
				.filter(gitFileInfo -> gitFileInfo.getPath().startsWith("changelog"))
				.collect(Collectors.toList());

		// 开始逐个 widget 解析，一个 widget 下包含多个日志文件，要先根据文件名进行排序
		// 然后按顺序，累加解析
		// 确保 widget 的排序不变，所以使用 LinkedHashMap
		this.allWidgetChangelogFiles = GitUtils
				.loadDataFromTag(store.getRepoSourceDirectory(), tag, allJsonFile)
				.stream()
				.collect(Collectors.groupingBy(
						jsonFile -> jsonFile.getPath().split("/")[1], 
						LinkedHashMap::new,
						Collectors.toList()));
	}

}