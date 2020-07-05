package com.blocklang.marketplace.apirepo;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blocklang.core.git.GitBlobInfo;
import com.blocklang.core.git.GitFileInfo;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.JsonSchemaValidator;
import com.blocklang.core.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

/**
 * 对 Ref 中所有 changelog 的 json schema 进行校验
 * 
 * @author Zhengwei Jin
 *
 */
public class RefChangelogSchemaValidator {

	private CliLogger logger;
	private JsonSchemaValidator validator;
	
	public RefChangelogSchemaValidator(CliLogger logger, JsonSchemaValidator validator) {
		this.logger = logger;
		this.validator = validator;
	}
	
	/**
	 * 校验 changelog 文件中的内容是否有效的 json 格式，以及是否遵循指定的 JSON Schema。
	 * 
	 * <p>不是遇见错误就退出，而是所有文件都要校验一遍</p>
	 * 
	 * @return 如果校验时出错，则返回 <code>false<code>；如果校验全部通过，则返回 <code>true</code>
	 */
	public boolean isValid(LinkedHashMap<String, List<GitBlobInfo>> allChangelogFiles) {
		boolean allValid = true;
		
		for(Map.Entry<String, List<GitBlobInfo>> entry : allChangelogFiles.entrySet()) {
			List<GitBlobInfo> changelogFiles = entry.getValue();
			changelogFiles.sort(Comparator.comparing(GitFileInfo::getName));

			for(GitBlobInfo fileInfo : changelogFiles) {
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
}
