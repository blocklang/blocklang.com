package com.blocklang.marketplace.apirepo;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.blocklang.core.git.GitBlobInfo;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.apirepo.apiobject.ApiObject;
import com.blocklang.marketplace.apirepo.schema.ApiSchemaData;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.data.RepoConfigJson;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 读取 Git ref 中的配置信息和所有 API 等
 * 
 * @author Zhengwei Jin
 *
 */
public class RefReader<T extends ApiObject> {
	
	private MarketplaceStore store;
	private CliLogger logger;
	
	private String gitUrl;
	private String shortRefName;
	private String fullRefName;
	private Integer createUserId;
	private Class<T> apiObjectClass;
	
	public RefReader(MarketplaceStore store, CliLogger logger, Class<T> apiObjectClass) {
		this.store = store;
		this.logger = logger;
		this.apiObjectClass = apiObjectClass;
	}
	
	public void setup(String gitUrl, String shortRefName, String fullRefName, Integer createUserId) {
		this.gitUrl = gitUrl;
		this.shortRefName = shortRefName;
		this.fullRefName = fullRefName;
		this.createUserId = createUserId;
	}

	public RefData<T> read() {
		RefData<T> result = new RefData<>();
		
		result.setGitUrl(gitUrl);
		result.setShortRefName(shortRefName);
		result.setFullRefName(fullRefName);
		result.setCreateUserId(createUserId);
		
		try {
			result.setRepoConfig(this.readRepoConfig());
			result.setSchemas(this.readSchemas());
			result.setApiObjects(this.readApiObjects());
		} catch (IOException e) {
			// 如果读这两项数据中的任一项出错了，就结束后面的保存操作。
			result.readFailed();
		}

		return result;
	}

	private RepoConfigJson readRepoConfig() throws JsonProcessingException {
		Optional<GitBlobInfo> fileOption = GitUtils.getBlob(store.getRepoSourceDirectory(), fullRefName, MarketplaceStore.BLOCKLANG_JSON);
		if(fileOption.isEmpty()) {
			logger.error("在 {0} 中未找到 {1} 文件", fullRefName, MarketplaceStore.BLOCKLANG_JSON);
			return null;
		}
		
		try {
			return JsonUtil.fromJsonObject(fileOption.get().getContent(), RepoConfigJson.class);
		} catch (JsonProcessingException e) {
			logger.error(e);
			throw e;
		}
	}
	
	private List<ApiSchemaData> readSchemas() throws IOException {
		try {
			List<ApiSchemaData> result = new ArrayList<>();
			Files.walkFileTree(store.getPackageSchemaDirectory(shortRefName), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if(attrs.isRegularFile() && "index.json".equals(file.getFileName().toString())) {
						String content = Files.readString(file);
						result.add(JsonUtil.fromJsonObject(content, ApiSchemaData.class));
					}
					return super.visitFile(file, attrs);
				}
			});
			return result;
		} catch(IOException e) {
			logger.error(e);
			throw e;
		}
	}
	
	private List<T> readApiObjects() throws IOException {
		try {
			List<T> result = new ArrayList<>();
			Files.walkFileTree(store.getPackageVersionDirectory(shortRefName), new SimpleFileVisitor<Path>() {
				
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					// 忽略 __schemas__ 目录下的内容
					if(dir.getFileName().toString().equals("__schemas__")) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					
					return super.preVisitDirectory(dir, attrs);
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if(attrs.isRegularFile() && "index.json".equals(file.getFileName().toString())) {
						String content = Files.readString(file);
						result.add(JsonUtil.fromJsonObject(content, apiObjectClass));
					}
					return super.visitFile(file, attrs);
				}
			});
			return result;
		} catch (IOException e) {
			logger.error(e);
			throw e;
		}
	}

}
