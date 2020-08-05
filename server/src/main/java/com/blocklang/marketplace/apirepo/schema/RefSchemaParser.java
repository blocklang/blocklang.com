package com.blocklang.marketplace.apirepo.schema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import com.blocklang.core.git.GitBlobInfo;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.JsonSchemaValidator;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.apirepo.ChangedObjectParser;
import com.blocklang.marketplace.apirepo.ApiRepoPathReader;
import com.blocklang.marketplace.apirepo.ParseResult;
import com.blocklang.marketplace.apirepo.PublishedFileInfo;
import com.blocklang.marketplace.apirepo.RefChangelogNameValidator;
import com.blocklang.marketplace.apirepo.RefChangelogSchemaValidator;
import com.blocklang.marketplace.data.MarketplaceStore;

public class RefSchemaParser {
	
	private MarketplaceStore store;
	private String fullRefName;
	private String shortRefName;
	private CliLogger logger;
	
	private LinkedHashMap<String, List<GitBlobInfo>> allChangelogFiles;
	private JsonSchemaValidator jsonSchemaValidator = new SchemaChangeSetSchemaValidator();
	private ApiRepoPathReader pathReader = new ApiRepoPathReader();
	private SchemaContext schemaContext;
	
	public RefSchemaParser(MarketplaceStore store, CliLogger logger, String fullRefName, String shortRefName, List<String> tags) {
		this.store = store;
		this.logger = logger;
		this.fullRefName = fullRefName;
		this.shortRefName = shortRefName;
		this.schemaContext = new SchemaContext(store, logger);
		this.schemaContext.setTags(tags);
	}

	public ParseResult run() {
		this.schemaContext.init(fullRefName, shortRefName);
		
		createSchemaDirectory();
		
		this.allChangelogFiles = readAllChangelogFiles();
		
		if(notFoundAnyChangelogFiles()) {
			logger.info("没有找到 schema 文件，不解析");
			return ParseResult.ABORT;
		}
		
		if(!validateDirAndFileName()) {
			return ParseResult.FAILED;
		}
		
		if(!validateJsonSchema()) {
			return ParseResult.FAILED;
		}
		
		if(publishedChangelogFileUpdated()) {
			return ParseResult.FAILED;
		}
		
		if(!parseAllSchemes()) {
			return ParseResult.FAILED;
		}
		
		if(!saveAllSchemas()) {
			return ParseResult.FAILED;
		}
		
		return ParseResult.SUCCESS;
	}

	// 创建 __schemas__ 文件夹
	private ParseResult createSchemaDirectory() {
		Path schemasPath = schemaContext.getChangedObjectPath(shortRefName);
		if(Files.notExists(schemasPath)) {
			try {
				Files.createDirectories(schemasPath);
			} catch (IOException e) {
				logger.error(e);
				return ParseResult.FAILED;
			}
		}
		return ParseResult.SUCCESS;
	}

	/**
	 *<pre> 
	 * 读取 /changelog/schemas/{schama}/*.json 目录下的所有 json 文件
	 * </pre>
	 */
	private LinkedHashMap<String, List<GitBlobInfo>> readAllChangelogFiles() {
		PathFilter pathFilter = PathFilter.create("changelog/schemas");
		PathSuffixFilter pathSuffixFilter = PathSuffixFilter.create(".json");
		TreeFilter filter = AndTreeFilter.create(pathFilter, pathSuffixFilter);
		
		return GitUtils.readAllFileContent(store.getRepoSourceDirectory(), fullRefName, filter).stream()
				.collect(Collectors.groupingBy(
						// changelog/schemas/{schema}/*.json
						// 2 表示 {schema}
						fileInfo -> fileInfo.getPath().split("/")[2], 
						LinkedHashMap::new,
						Collectors.toList()));
	}
	
	private boolean notFoundAnyChangelogFiles() {
		if(this.allChangelogFiles.isEmpty()) {
			return true;
		}
		return this.allChangelogFiles
				.entrySet()
				.stream()
				.allMatch(entry -> entry.getValue().isEmpty());
	}
	
	private boolean validateDirAndFileName() {
		var validator = new RefChangelogNameValidator(logger);
		return validator.isValid(allChangelogFiles);
	}

	/**
	 * 校验 changelog 文件中的内容是否有效的 json 格式，以及是否遵循指定的 JSON Schema。
	 * 
	 * <p> 注意：不是遇见错误就退出，而是所有文件都要校验一遍</p>
	 * 
	 * @return 如果校验时出错，则返回 <code>false<code>；如果校验全部通过，则返回 <code>true</code>
	 */
	private boolean validateJsonSchema() {
		var validator = new RefChangelogSchemaValidator(logger, jsonSchemaValidator);
		return validator.isValid(allChangelogFiles);
	}
	
	private boolean publishedChangelogFileUpdated() {
		boolean hasPublishedChangeLogFileUpdated = false;

		for(Map.Entry<String, List<GitBlobInfo>> entry : allChangelogFiles.entrySet()) {
			String directoryName = entry.getKey();
			List<GitBlobInfo> changelogFiles = entry.getValue();
			
			String schemaId = pathReader.read(directoryName).getOrder();
			schemaContext.setObjectId(schemaId);
			List<PublishedFileInfo> publishedFiles = getPublishedChangelogFiles(schemaId);
			
			for(GitBlobInfo file : changelogFiles) {
				String fileId = pathReader.read(file.getName()).getOrder();
				Optional<PublishedFileInfo> publishedFileOption = publishedFiles
						.stream()
						.filter(changelog -> changelog.getFileId().equals(fileId))
						.findFirst();
				if(publishedFileOption.isPresent()) {
					String md5sumPublished = publishedFileOption.get().getMd5sum();
					String md5sumNow = DigestUtils.md5Hex(file.getContent());
					if(!md5sumPublished.equals(md5sumNow)) {
						logger.error("{0}/{1} 已被修改，已应用版本的 checksum 为 {2}，但当前版本的 checksum 为 {3}", 
								directoryName,
								file.getName(), 
								md5sumPublished, 
								md5sumNow);
						hasPublishedChangeLogFileUpdated = true;
					}
				}
			}
			
			if(!hasPublishedChangeLogFileUpdated) {
				schemaContext.addPublishedChangelogFiles(publishedFiles);
			}
		}
		
		return hasPublishedChangeLogFileUpdated;
	}

	private List<PublishedFileInfo> getPublishedChangelogFiles(String schemaId) {
		List<PublishedFileInfo> result = new ArrayList<>();
		Path changelogPath = store.getPackageSchemaChangelogDirectory().resolve(schemaId).resolve("index.json");
		try {
			String changelogJson = Files.readString(changelogPath);
			List<PublishedFileInfo> published = JsonUtil.fromJsonArray(changelogJson, PublishedFileInfo.class);
			
			// 因为 master 分支每次都要重新解析，所以清除 master 分支的解析记录
			published.removeIf(fileInfo -> fileInfo.getVersion().equals("master"));
			result.addAll(published);
		} catch (IOException e1) {
			// 如果文件不存在，则使用空 List
		}
		return result;
	}
	
	private boolean parseAllSchemes() {
		boolean success = true;
		var changeParserFactory = new SchemaChangeParserFactory(logger);
		var apiObjectParser = new ChangedObjectParser(changeParserFactory);
		
		for(Map.Entry<String, List<GitBlobInfo>> entry : allChangelogFiles.entrySet()) {
			List<GitBlobInfo> changelogFiles = entry.getValue();
			String schemaId = pathReader.read(entry.getKey()).getOrder();
			schemaContext.setObjectId(schemaId);
			schemaContext.loadPreviousVersionObject();
			success = apiObjectParser.run(schemaContext, changelogFiles);
		}
		return success;
	}

	private boolean saveAllSchemas() {
		return schemaContext.saveAllChangedObjects(shortRefName);
	}
}
