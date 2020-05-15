package com.blocklang.core.runner.action;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.runner.common.TaskLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.RepoType;
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
 * inputs
 *     configFile   - Path(required)，blocklang.json 文件路径
 * outputs
 *     repoConfig   - Object，blocklang.json 文件中的内容
 * </pre>
 * 
 * @author Zhengwei Jin
 * 
 * FIXME: 此类是 marketplace 专用的，不应放在 core 包中，应该移到 marketplace 包中
 *
 */
public class GetRepoConfigAction extends AbstractAction{
	
	private static final String CONFIG_FILE_NAME = "blocklang.json";
	
	// FIXME： 如何确保 input 和 output 变量名在 context 中全局唯一？
	public static final String INPUT_CONFIG_FILE = "configFile";
	public static final String OUTPUT_REPO_CONFIG = "repoConfig"; 
	
	private Path configFile;

	public GetRepoConfigAction(ExecutionContext context) {
		super(context);
		
		this.configFile = context.getValue(INPUT_CONFIG_FILE, Path.class);
		Assert.notNull(this.configFile, "必须要设置 " + INPUT_CONFIG_FILE + "参数!");
	}

	/**
	 * <ul>
	 * <li> 从 master 分支读取 blocklang.json 文件
	 * <li> 转换为 JsonNode 对象
	 * <li> 使用 schema 进行格式校验
	 * <li> 校验通过后，转换为 RepoConfigJson 对象
	 * <li> 往 output 中传 RepoConfigJson 对象
	 * </ul>
	 */
	@Override
	public Optional<?> run() {
		// 前置条件，确保当前分支是 master 分支
		String content = "";
		logger.info("读取仓库中的 {0} 文件", CONFIG_FILE_NAME);
		try {
			content = Files.readString(this.configFile);
			logger.info("{0} 完成", TaskLogger.ANSWER);
		} catch (IOException e) {
			logger.error(e);
			return Optional.empty();
		}
		
		logger.info("校验 {0} 文件格式", CONFIG_FILE_NAME);
		JsonNode jsonNode;
		try {
			jsonNode = JsonUtil.readTree(content);
		} catch (JsonProcessingException e) {
			logger.error(e);
			return Optional.empty();
		}
		
		// 先判断类型
		String repo = jsonNode.get("repo").asText();
		String category = jsonNode.get("category").asText();
		// 根据 repo 和 category 定义不同的 json schema 文件
		// 定义一个 json schema 校验 json 格式
		if(RepoType.IDE.getValue().equals(repo) && RepoCategory.WIDGET.getValue().equals(category)) {
			String schemaFileName = "widget_ide_repo_config_schema.json";
			String schema;
			try {
				schema = StreamUtils.copyToString(getClass().getResourceAsStream(schemaFileName), Charset.defaultCharset());
			} catch (IOException e) {
				logger.error(e);
				return Optional.empty();
			}
			
			Set<ValidationMessage> errors = JsonUtil.validate(jsonNode, schema);
			if(!errors.isEmpty()) {
				logger.error("{0} 格式有误，请按以下说明调整后再注册", CONFIG_FILE_NAME);
				errors.forEach(error -> logger.error(error.getMessage()));
				return Optional.empty();
			}
		}
		
		RepoConfigJson config;
		try {
			config = JsonUtil.treeToValue(jsonNode, RepoConfigJson.class);
		} catch (JsonProcessingException e) {
			logger.error(e);
			return Optional.empty();
		}
		
		context.putValue(OUTPUT_REPO_CONFIG, config);
		return Optional.of(true);
	}

}
