package com.blocklang.marketplace.apiparser;

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
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import com.blocklang.core.git.GitBlobInfo;
import com.blocklang.core.git.GitFileInfo;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.JsonSchemaValidator;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.apiparser.widget.WidgetOperator;
import com.blocklang.marketplace.apiparser.widget.WidgetOperatorContext;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.runner.action.PublishedFileInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

public abstract class AbstractRefParser {

	
	protected Map<String, List<PublishedFileInfo>> allPublishedFiles = new HashMap<String, List<PublishedFileInfo>>();
	protected LinkedHashMap<String, List<GitBlobInfo>> allGroupedChangelogFiles;
	protected JsonSchemaValidator validator;
	protected MarketplaceStore store;
	protected CliLogger logger;
	protected List<String> tags;
	
	protected WidgetOperatorContext operatorContext;
	protected ApiRepoPathReader pathReader = new ApiRepoPathReader();
	
	public AbstractRefParser(List<String> tags, MarketplaceStore store, CliLogger logger) {
		this.tags = tags;
		this.store = store;
		this.logger = logger;
	}
	
	public void setChangeSetSchemaValidator(JsonSchemaValidator validator) {
		this.validator = validator;
	}

	public void setTags(List<String> tags) {
		// TODO Auto-generated method stub
		
	}

	protected void readAllChangelogs(String fullRefName) {
		PathSuffixFilter pathSuffixFilter = PathSuffixFilter.create(".json");
		PathFilter pathFilter = PathFilter.create("changelog");
		this.allGroupedChangelogFiles = GitUtils
			.readAllFiles(
				store.getRepoSourceDirectory(), 
				fullRefName, 
				AndTreeFilter.create(pathFilter, pathSuffixFilter))
			.stream()
			.collect(Collectors.groupingBy(
				// path 中 0 是 changelog，1 是 分组名
				fileInfo -> fileInfo.getPath().split("/")[1], 
				LinkedHashMap::new, 
				Collectors.toList()));
	}
	
	protected boolean hasChangelogs() {
		if(this.allGroupedChangelogFiles.isEmpty()) {
			return false;
		}
		
		return this.allGroupedChangelogFiles.entrySet()
				.stream()
				.anyMatch(entry -> !entry.getValue().isEmpty());
	}

	// 不是遇见错误就退出，而是所有文件都要校验一遍
	protected boolean validateJsonSchema() {
		boolean allValid = true;
		
		for(Map.Entry<String, List<GitBlobInfo>> entry : allGroupedChangelogFiles.entrySet()) {
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
					Set<ValidationMessage> errors = validator.run(jsonContent);
					if (!errors.isEmpty()) {
						allValid = false;
						errors.forEach(error -> logger.error(error.getMessage()));
					}
				}
			}
			
		}
		return allValid;
	}

	protected boolean publishedFileUpdated() {
		boolean hasPublishedChangeLogUpdated = false;
		
		for(Map.Entry<String, List<GitBlobInfo>> entry : allGroupedChangelogFiles.entrySet()) {
			String directoryName = entry.getKey();
			List<GitBlobInfo> changelogFiles = entry.getValue();
			
			String dirId = pathReader.read(directoryName).getOrder();
			List<PublishedFileInfo> changeLogs = getPublishedFiles(dirId);

			// 校验已发布的文件是否被修改过
			for (GitBlobInfo jsonFile : changelogFiles) {
				String jsonFileId = pathReader.read(jsonFile.getName()).getOrder();
				// 判断该文件是否已执行过
				Optional<PublishedFileInfo> changeLogInfoOption = changeLogs.stream()
						.filter(changeLog -> changeLog.getFileId().equals(jsonFileId)).findFirst();

				// 如果已执行过，则跳过
				if (changeLogInfoOption.isPresent()) {
					// 如果已执行过，但文件内容已修改，则给出错误提示
					String md5sumPublished = changeLogInfoOption.get().getMd5sum();
					String md5sumNow = DigestUtils.md5Hex(jsonFile.getContent());
					if (!md5sumPublished.equals(md5sumNow)) {
						logger.error("{0}/{1} 已被修改，已应用版本的 checksum 为 {2}，但当前版本的 checksum 为 {3}", directoryName,
								jsonFile.getName(), md5sumPublished, md5sumNow);
						hasPublishedChangeLogUpdated = true;
					}
				}
			}
		}
		return hasPublishedChangeLogUpdated;
	}
	
	protected abstract boolean parseAllApi(String fullRefName);

	protected abstract boolean saveAllApi(String shortRefName);
	
	protected abstract List<PublishedFileInfo> getPublishedFiles(String dirId);
	
	/**
	 * 获取上一个 tag 中发布的 Widget
	 * 
	 * @param branch      git branch name
	 * @param widgetId widget 标识，取文件名 202005161723_button 中的时间戳
	 * @return 获取上一个 tag 中发布的 Widget，如果不存在上一个 tag，则返回 <code>null</code>
	 */
	protected abstract <T> T loadPreviousVersion(String fullRefName, String groupId, Class<T> clazz);

	// 要全都校验一遍
	// 
	// 在此处校验分组和 changelog 文件的命名规范
	// 格式为 {order}_{description}，或者 {order}__，或者 {order}
	// order 必须为 yyyyMMddHHmm 时间戳，并且一个文件夹下不会重复
	// 同一层的目录名中的 order 不能重复
	// 同一层的文件名中的 order 不能重复
	protected boolean validateFileNamePattern() {
		boolean result = true;
		List<ApiRepoPathInfo> group = new ArrayList<ApiRepoPathInfo>();
		
		for(Map.Entry<String, List<GitBlobInfo>> entry : this.allGroupedChangelogFiles.entrySet()) {
			String foldName = entry.getKey();
			List<String> errors = pathReader.validate(foldName);
			
			if(!errors.isEmpty()) {
				result = false;
			}
			for(String error : errors) {
				logger.error("文件夹名称 {0} 不符合规范 {1}", foldName, error);
			}
			
			group.add(pathReader.read(foldName));
		
			// 校验分组下的 changelog 文件名
			List<ApiRepoPathInfo> files = new ArrayList<ApiRepoPathInfo>();
			for(GitBlobInfo fileInfo : entry.getValue()) {
				String fileName = fileInfo.getName();
				List<String> fileErrors = pathReader.validate(fileName);
				if(!fileErrors.isEmpty()) {
					result = false;
				}
				for(String error : fileErrors) {
					logger.error("changelog 文件名称 {0} 不符合规范 {1}", fileName, error);
				}
				files.add(pathReader.read(fileName));
			}
			
			Map<String, Long> duplicatedFileName = files.stream().collect(Collectors.groupingBy(path -> path.getOrder(), Collectors.counting()));
			for(Map.Entry<String, Long> item : duplicatedFileName.entrySet()) {
				if(item.getValue() > 1) {
					result = false;
					logger.error("{0} 被用了 {1} 次", item.getKey(), item.getValue());
				}
			}
		}
		
		Map<String, Long> duplicatedGroupName = group.stream().collect(Collectors.groupingBy(path -> path.getOrder(), Collectors.counting()));
		for(Map.Entry<String, Long> item : duplicatedGroupName.entrySet()) {
			if(item.getValue() > 1) {
				result = false;
				logger.error("{0} 被用了 {1} 次", item.getKey(), item.getValue());
			}
		}
		
		return result;
	}
	
	protected List<WidgetOperator> readChangesInOneFile(GitBlobInfo jsonFile) {
		// 一次处理一个文件中的变更，而不是将所有所有文件中的变更
		List<WidgetOperator> changes = new ArrayList<>();
		// 从 json 中获取 changes 列表中的内容，并转换为对应的操作，先实现 createWidget 操作
		OperatorFactory factory = new OperatorFactory(logger);
		try {
			JsonNode jsonNode = JsonUtil.readTree(jsonFile.getContent());
			JsonNode changeNodes = jsonNode.get("changes");
			for (JsonNode changeNode : changeNodes) {
				WidgetOperator operator = factory.create(changeNode);
				if(operator == null) {
					break;
				}
				changes.add(operator);
			}
		} catch (JsonProcessingException e) {
			// do nothing
		}
		return changes;
	}

}
