package com.blocklang.marketplace.apirepo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.task.CodeGenerator;

public class ApiObjectContext {

	private static final String MASTER_REF = "refs/heads/master";
	private List<String> tags;
	private MarketplaceStore store;
	private CliLogger logger;

	private CodeGenerator codeGenerator;
	
	private Map<String, List<PublishedFileInfo>> allPublishedChangelogFiles;
	private List<ApiObject> apiObjects = new ArrayList<>();
	private ApiObject current;
	private String apiObjectId;
	
	private String fullRefName;
	private String shortRefName;
	
	public ApiObjectContext(MarketplaceStore store, CliLogger logger) {
		this.store = store;
		this.logger = logger;
	}
	
	/**
	 * 只需为在初始化 context 时设置一次
	 * @param tags
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	/**
	 * 初始化上下文中的信息，这样就可以在多个 ref 之间共享上下文信息。
	 * 
	 * 在每次调用 refParser.run 前调用
	 * 
	 * @param fullRefName
	 * @param shortRefName
	 */
	public void init(String fullRefName, String shortRefName) {
		this.fullRefName = fullRefName;
		this.shortRefName = shortRefName;
		
		allPublishedChangelogFiles = new HashMap<String, List<PublishedFileInfo>>();
		apiObjects = new ArrayList<>();
		current = null;
		apiObjectId = null;
		codeGenerator = null;
	}

	public CliLogger getLogger() {
		return this.logger;
	}
	
	/**
	 * 在开始解析每个 ApiObject 时调用
	 * 
	 * @param apiObjectId
	 */
	public void setApiObjectId(String apiObjectId) {
		this.apiObjectId = apiObjectId;
	}
	
	public String getApiObjectId() {
		return this.apiObjectId;
	}

	public List<PublishedFileInfo> getPublishedChangelogFiles() {
		var result = allPublishedChangelogFiles.get(apiObjectId);
		if(result == null) {
			return Collections.emptyList();
		}
		return result;
	}
	
	public void addPublishedChangelogFiles(List<PublishedFileInfo> publishedFiles) {
		allPublishedChangelogFiles.put(apiObjectId, publishedFiles);
	}

	public void addApiObject(ApiObject apiObject) {
		this.apiObjects.add(apiObject);
		this.current = apiObject;
	}

	public void loadPreviousVersionApiObject() {
		if(tags.isEmpty()) {
			// 不存在已解析的 ApiObject
			return;
		}

		int preTagIndex = getPreviousTagIndex();
		if(preTagIndex < 0) {
			return;
		}
		
		String preVersion = GitUtils.getVersionFromRefName(tags.get(preTagIndex)).get();
		Path apiObjectPath = store.getPackageVersionDirectory(preVersion)
			.resolve(apiObjectId)
			.resolve("index.json");
		if(!apiObjectPath.toFile().exists()) {
			// 如果组件是在 tag 中新增的，则获取不到上一版本的 index.json 属于正常情况
			return;
		}

		try {
			String content = Files.readString(apiObjectPath);
			ApiObject apiObject = JsonUtil.fromJsonObject(content, getApiObjectClass());
			this.addApiObject(apiObject);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public String getShortRefName() {
		return this.shortRefName;
	}
	
	private int getPreviousTagIndex() {
		if(MASTER_REF.equals(fullRefName)) {
			return tags.size() - 1;
		}
		return tags.indexOf(fullRefName) - 1;
	}

	protected Class<? extends ApiObject> getApiObjectClass() {
		return null;
	}

	public boolean changelogFileParsed(String fileId) {
		return getPublishedChangelogFiles()
			.stream()
			.anyMatch(changelog -> changelog.getFileId().equals(fileId));
	}

	public void addParsedChangelogFile(PublishedFileInfo parsedFile) {
		getPublishedChangelogFiles().add(parsedFile);
	}

	public boolean saveAllApiObject(String shortRefName) {
		for(ApiObject apiObject : apiObjects) {
			Path path = store.getPackageVersionDirectory(shortRefName).resolve(apiObject.getId());
			try {
				Files.createDirectories(path);
				Files.writeString(path.resolve("index.json"), JsonUtil.stringify(apiObject));
			} catch (IOException e) {
				logger.error(e);
				return false;
			}
		}
		
		for(Map.Entry<String, List<PublishedFileInfo>> each : allPublishedChangelogFiles.entrySet()) {
			Path widgetChangelogDirectory = store.getPackageChangeLogDirectory().resolve(each.getKey());
			try {
				Files.createDirectories(widgetChangelogDirectory);
				Files.writeString(widgetChangelogDirectory.resolve("index.json"), JsonUtil.stringify(each.getValue()));
			} catch (IOException e) {
				logger.error(e);
				return false;
			}
		}
		
		return true; 
	}
	
	/**
	 * 用于判断 api object 的 name 是否被占用
	 * 
	 * @param name api object name
	 * @return 如果 name 被占用，则返回 <code>true</code>；否则返回 <code>false</code>
	 */
	public boolean apiObjectNameUsed(String name) {
		return this.apiObjects.stream().anyMatch(w -> w.getName().equals(name));
	}

	/**
	 * 获取下一个可用的 Api Object code
	 * 
	 * @return 下一个可用的 code
	 */
	public String nextApiObjectCode() {
		if(codeGenerator == null) {
			String seed = apiObjects.isEmpty() ? null : apiObjects.get(apiObjects.size() - 1).getCode();
			codeGenerator = new CodeGenerator(seed);
		}
		return codeGenerator.next();
	}

	public List<ApiObject> getApiObjects() {
		return this.apiObjects;
	}

	public ApiObject getSelectedApiObject() {
		return this.current;
	}
	
}
