package com.blocklang.marketplace.runner.action;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.blocklang.core.git.GitBlobInfo;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.RepoType;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.data.RepoConfigJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

/**
 * 读取 git 仓库根目录下的 blocklang.json 文件。
 * 
 * <ul>
 * <li> 读取 blocklang.json 文件
 * <li> 对 blocklang.json 的 schema 进行校验
 * </ul>
 * 
 * <pre>
 * outputs
 *     repoConfig   - Object，blocklang.json 文件中的内容
 * </pre>
 * 
 * configFile 是配置文件 blocklang.json 的路径，从 store 中获取
 * 
 * @author Zhengwei Jin
 * 
 * FIXME: 此类是 marketplace 专用的，不应放在 core 包中，应该移到 marketplace 包中
 *
 */
public class GetRepoConfigAction extends AbstractAction{
	
	public static final String OUTPUT_REPO_CONFIG = "repoConfig"; 
	
	private String configFileName;
	private Path sourceDirectory;
	
	private RepoConfigJson repoConfigJson;

	public GetRepoConfigAction(ExecutionContext context) {
		super(context);
		
		MarketplaceStore store = context.getValue(ExecutionContext.MARKETPLACE_STORE, MarketplaceStore.class);
		this.sourceDirectory = store.getRepoSourceDirectory();
		this.configFileName = MarketplaceStore.BLOCKLANG_JSON;
	}

	/**
	 * <ul>
	 * <li> 从 master 分支读取 blocklang.json 文件，但要跟所有 tag 对比，确保 repo 和 category 的值没有改变
	 * <li> 转换为 JsonNode 对象
	 * <li> 使用 schema 进行格式校验
	 * <li> 校验通过后，转换为 RepoConfigJson 对象
	 * <li> 往 output 中传 RepoConfigJson 对象
	 * </ul>
	 */
	@Override
	public boolean run() {
		// 前置条件，确保当前分支是 master 分支
		// 改为读取所有 tag 和 master 分支中的 blocklang 文件
		// 并校验其中的 repo 和 category 中的值是否相同
		// 如果不同，则给出错误信息
		
		// 1. 获取所有 tag 中的 blocklang.json 文件内容
		List<Pair<String, GitBlobInfo>> blocklangContents = readConfigFromTagsAndMasterBranch();
		if(blocklangContents.isEmpty()) {
			logger.error("在 git 仓库中没有找到 master 分支");
			return false;
		}

		List<Integer> errorCount = new ArrayList<Integer>();
		
		// 确保将所有文件的所有逻辑都校验一遍，即找出所有可能错误，而不是校验一种错误就退出。
		List<Pair<String, RepoConfigJson>> configs = blocklangContents.stream().map(pair -> {
			// 校验如果有分支中缺少 blocklang.json 文件，则给出错误信息
			if(pair.getValue() == null) {
				logger.info("{0} 中没有找到 {1} 文件", pair.getKey(), MarketplaceStore.BLOCKLANG_JSON);
				errorCount.add(1);
			}
			return pair;
		}).map(pair -> {
			Pair<String, RepoConfigJson> errorResult = Pair.of(pair.getKey(), null);
			if(pair.getValue() == null) {
				return errorResult;
			}
			
			logger.info("校验 {0} 文件格式", MarketplaceStore.BLOCKLANG_JSON);
			JsonNode jsonNode;
			try {
				jsonNode = JsonUtil.readTree(pair.getValue().getContent());
			} catch (JsonProcessingException e) {
				logger.error(e);
				errorCount.add(1);
				return errorResult;
			}
			
			// 先判断类型
			String repo = jsonNode.get("repo").asText();
			String category = jsonNode.get("category").asText();
			
			if(!validateRepoTypeAndCategory(repo, category)) {
				errorCount.add(1);
				return errorResult;
			}
			
			Set<ValidationMessage> errors = BlocklangJsonValidator.run(jsonNode);
			if(!errors.isEmpty()) {
				logger.error("{0} 格式有误，请按以下说明调整后再注册", configFileName);
				errors.forEach(error -> logger.error(error.getMessage()));
				errorCount.add(1);
				return errorResult;
			}
			
			try {
				RepoConfigJson repoConfigJson = JsonUtil.treeToValue(jsonNode, RepoConfigJson.class);
				return Pair.of(pair.getKey(), repoConfigJson);
			} catch (JsonProcessingException e) {
				logger.error(e);
				errorCount.add(1);
				return errorResult;
			}
			
		}).filter(pair -> pair.getValue() != null).collect(Collectors.toList());
		
		// 校验所有文件中的 repo 和 category 值是否相同
		if(configs.size() > 1) {
			RepoConfigJson firstConfig = configs.get(0).getValue();
			String repo = firstConfig.getRepo();
			String category = firstConfig.getCategory();
			
			for(var config : configs) {
				String curRepo = config.getValue().getRepo();
				if(!curRepo.equals(repo)) {
					logger.error("一经发布，repo 的值不允许改变，但 {0} 下的 repo 值却改为 {1}，应改回 {2}", config.getKey(), curRepo, repo);
					errorCount.add(1);
				}
				
				String curCategory = config.getValue().getCategory();
				if(!curCategory.equals(category)) {
					logger.error("一经发布，category 的值不允许改变，但 {0} 下的 category 值却改为 {1}，应改回 {2}", config.getKey(), curCategory, category);
					errorCount.add(1);
				}
			}
		}
		
		if(!errorCount.isEmpty()) {
			return false;
		}
		
		repoConfigJson = configs.get(configs.size() - 1).getValue();
		return true;
	}

	private boolean validateRepoTypeAndCategory(String repo, String category) {
		boolean success = true;
		if(RepoType.fromValue(repo) == null) {
			String types = Arrays.stream(RepoType.values()).map(type -> type.getValue()).collect(Collectors.joining(","));
			logger.error("repo 的值只能取 [{0}] 中的一个值", types);
			success = false;
		}
		
		if(RepoCategory.fromValue(category) == RepoCategory.UNKNOWN) {
			String categories = Arrays.stream(RepoCategory.values())
					.filter(item -> item != RepoCategory.UNKNOWN)
					.map(item -> item.getValue()).collect(Collectors.joining(","));
			logger.error("category 的值只能取 [{0}] 中的一个值", categories);
			success = false;
		}
		
		// Service 只支持 API 仓库
		if(repo.equals("Service") && !category.equals("API")) {
			logger.error("category 为 Service 时，repo 的值只能是 API，但现在的值为 {0}", category);
			success = false;
		}
		
		return success;
	}

	private List<Pair<String, GitBlobInfo>> readConfigFromTagsAndMasterBranch() {
		List<Pair<String, GitBlobInfo>> blocklangContents = GitUtils
				.getTags(this.sourceDirectory)
				.stream()
				.map(ref -> {
					String tagName = ref.getName();
					GitBlobInfo blobInfo = GitUtils.getBlob(this.sourceDirectory, ref.getName(), MarketplaceStore.BLOCKLANG_JSON).orElse(null);
					return Pair.of(tagName, blobInfo);
				})
				.collect(Collectors.toList());
		
		String master = "refs/heads/master";
		GitBlobInfo masterBlocklangContent = GitUtils.getBlob(this.sourceDirectory, master, MarketplaceStore.BLOCKLANG_JSON).orElse(null);
		blocklangContents.add(Pair.of(master, masterBlocklangContent));
		return blocklangContents;
	}

	@Override
	protected Object getOutput(String paramKey) {
		if(OUTPUT_REPO_CONFIG.equals(paramKey)) {
			return repoConfigJson;
		}
		return super.getOutput(paramKey);
	}
	
}
