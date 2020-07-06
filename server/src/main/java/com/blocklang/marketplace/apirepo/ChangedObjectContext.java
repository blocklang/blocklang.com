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

/**
 * 存储某一类操作对象的上下文信息
 * 
 * @author Zhengwei Jin
 *
 */
public abstract class ChangedObjectContext {

	private static final String MASTER_REF = "refs/heads/master";
	private List<String> tags;

	private Map<String, List<PublishedFileInfo>> allPublishedChangelogFiles;
	
	private String fullRefName;
	private String shortRefName;
	private CodeGenerator codeGenerator;
	
	protected MarketplaceStore store;
	protected CliLogger logger;
	private List<ChangedObject> changedObjects = new ArrayList<>();
	private ChangedObject current;
	private String objectId;
	
	public ChangedObjectContext(MarketplaceStore store, CliLogger logger) {
		this.store = store;
		this.logger = logger;
	}
	
	public CliLogger getLogger() {
		return this.logger;
	}

	/**
	 * 用于判断 api object 或者 schema 的 name 是否被占用
	 * 
	 * @param name object name
	 * @return 如果 name 被占用，则返回 <code>true</code>；否则返回 <code>false</code>
	 */
	public boolean objectNameUsed(String objectName) {
		return this.changedObjects.stream().anyMatch(w -> w.getName().equals(objectName));
	}
	
	public void addObject(ChangedObject changedObject) {
		this.changedObjects.add(changedObject);
		this.current = changedObject;
	}

	public String getObjectId() {
		return objectId;
	}

	/**
	 * 在开始解析每个 ChangedObject 时调用
	 * 
	 * @param objectId
	 */
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	public List<ChangedObject> getChangedObjects() {
		return this.changedObjects;
	}
	
	/**
	 * 获取下一个可用的 Object code
	 * 
	 * @return 下一个可用的 code
	 */
	public String nextObjectCode() {
		if(codeGenerator == null) {
			String seed = null;
			if(!changedObjects.isEmpty()) {
				seed = changedObjects.get(changedObjects.size() - 1).getCode();
			}
			codeGenerator = new CodeGenerator(seed);
		}
		return codeGenerator.next();
	}

	public ChangedObject getSelectedObject() {
		return this.current;
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
		changedObjects = new ArrayList<>();
		current = null;
		objectId = null;
		codeGenerator = null;
	}

	public List<PublishedFileInfo> getPublishedChangelogFiles() {
		var result = allPublishedChangelogFiles.get(objectId);
		if(result == null) {
			return Collections.emptyList();
		}
		return result;
	}
	
	public void addPublishedChangelogFiles(List<PublishedFileInfo> publishedFiles) {
		allPublishedChangelogFiles.put(objectId, publishedFiles);
	}
	
	public String getShortRefName() {
		return this.shortRefName;
	}
	
	public void loadPreviousVersionObject() {
		if(tags.isEmpty()) {
			// 不存在已解析的 ChangedObject
			return;
		}

		int preTagIndex = getPreviousTagIndex();
		if(preTagIndex < 0) {
			return;
		}
		
		String preVersion = GitUtils.getVersionFromRefName(tags.get(preTagIndex)).get();
		Path changedObjectPath = getChangedObjectPath(preVersion)
			.resolve(objectId)
			.resolve("index.json");
		if(!changedObjectPath.toFile().exists()) {
			// 如果组件是在 tag 中新增的，则获取不到上一版本的 index.json 属于正常情况
			return;
		}

		try {
			String content = Files.readString(changedObjectPath);
			ChangedObject changedObject = JsonUtil.fromJsonObject(content, getChangedObjectClass());
			this.addObject(changedObject);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	protected abstract Path getChangedObjectPath(String previousVersion);
	
	protected int getPreviousTagIndex() {
		if(MASTER_REF.equals(fullRefName)) {
			return tags.size() - 1;
		}
		return tags.indexOf(fullRefName) - 1;
	}

	protected Class<? extends ChangedObject> getChangedObjectClass() {
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

	public boolean saveAllChangedObjects(String shortRefName) {
		for(ChangedObject changedObject : changedObjects) {
			Path path = getChangedObjectPath(shortRefName).resolve(changedObject.getId());
			try {
				Files.createDirectories(path);
				Files.writeString(path.resolve("index.json"), JsonUtil.stringify(changedObject));
			} catch (IOException e) {
				logger.error(e);
				return false;
			}
		}
		
		for(Map.Entry<String, List<PublishedFileInfo>> each : allPublishedChangelogFiles.entrySet()) {
			Path widgetChangelogDirectory = getPackageChangelogPath().resolve(each.getKey());
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

	protected abstract Path getPackageChangelogPath();
}
