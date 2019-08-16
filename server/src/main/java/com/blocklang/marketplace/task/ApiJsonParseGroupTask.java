package com.blocklang.marketplace.task;

import java.io.IOException;
import java.util.Optional;

import com.blocklang.core.git.GitUtils;
import com.blocklang.marketplace.constant.MarketplaceConstant;
import com.blocklang.marketplace.data.ApiJson;
import com.blocklang.marketplace.data.ComponentJson;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiJsonParseGroupTask extends AbstractRepoPublishTask {

	public ApiJsonParseGroupTask(MarketplacePublishContext context) {
		super(context);
	}

	@Override
	public Optional<Boolean> run() {
		ComponentJson componentJson = context.getComponentJson();
		if(componentJson == null) {
			// 如果执行了此处代码，则说明父分组任务中的代码有 bug
			logger.error("本任务的前置条件不满足，即 {0} 的值没有解析完毕", MarketplaceConstant.FILE_NAME_COMPONENT);
			return Optional.empty();
		}
		
		boolean success = true;
		String gitUrl = componentJson.getApi().getGit().trim();
		logger.info("开始校验 {0} 仓库是否存在", gitUrl);
		success = GitUtils.isValidRemoteRepository(gitUrl);
		if(success) {
			logger.info("存在");
		}else {
			logger.error("不存在");
		}
		
		// 从源代码托管网站下载 API 项目
		// 开始解析 api 项目
		if(success) {
			context.parseApiGitUrl(gitUrl);
			logger.info("开始下载 API 仓库源码");
			GitSyncApiRepoTask apiRepoTask = new GitSyncApiRepoTask(context);
			Optional<Boolean> gitSyncApiOption = apiRepoTask.run();
			success = gitSyncApiOption.isPresent();
			if(success) {
				logger.info("完成");
			} else {
				logger.error("失败");
			}
		}
		
		// 因为在后续保存 API 变更文件时，需要按照版本增量安装，所以这里获取 API 仓库的所有版本
		if(success) {
			logger.info("获取 API 仓库的所有版本号");
			ApiRepoVersionsFindTask apiRepoVersionsFindTask = new ApiRepoVersionsFindTask(context);
			success = apiRepoVersionsFindTask.run().isPresent();
		}
		
		// 确认在 component.json 中指定的 api 仓库的版本号是否存在
		String apiRepoRefName = null;
		if(success) {
			// 确认在 API 仓库中是否存在指定的版本
			String apiRepoVersion = componentJson.getApi().getVersion().trim();
			logger.info("检查 API 仓库中是否存在 {0} 发行版", apiRepoVersion);
			
			Optional<String> tagNameOption = context.getAllApiRepoRefNames()
					.stream().filter(tagName -> tagName.endsWith(apiRepoVersion))
					.findFirst();
			success = tagNameOption.isPresent();
			if(success) {
				apiRepoRefName = tagNameOption.get();
				context.setApiRepoRefName(apiRepoRefName);
				logger.info("存在");
			} else {
				logger.error("不存在");
			}
		}
		
		// 从指定的发行版中获取 api.json 内容
		String apiJsonContent = null;
		if(success) {
			logger.info("校验 API 仓库根目录是否存在 {0} 文件", MarketplaceConstant.FILE_NAME_API);
			// 从组件库的 component.json 中指定的 api 版本中查找
			ApiJsonFetchTask task = new ApiJsonFetchTask(context, apiRepoRefName);
			Optional<String> contentOption = task.run();
			success = contentOption.isPresent();
			if(success) {
				apiJsonContent = contentOption.get();
				logger.info("存在");
			} else {
				logger.error("不存在");
			}
		}
		
		// 将 json 字符串转换为 java 对象
		ApiJson apiJson = null;
		if(success) {
			logger.info("将 {0} 内容转换为 java 对象", MarketplaceConstant.FILE_NAME_API);
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				apiJson = objectMapper.readValue(apiJsonContent, ApiJson.class);
				context.setApiJson(apiJson);
				logger.info("转换完成");
				success = true;
			} catch (IOException e) {
				logger.error("转换失败");
				logger.error(e);
				success = false;
			}
		}
		
		// 校验 component.json 的 schema 和值的有效性
		if(success) {
			logger.info("校验 {0} 文件的 schema 和值", MarketplaceConstant.FILE_NAME_API);
			
			ApiJsonValidateTask apiJsonValidateTask = new ApiJsonValidateTask(context);
			success = apiJsonValidateTask.run().isPresent();
			if(success) {
				logger.info("校验通过");
			}else {
				logger.error("校验未通过");
			}
		}
		
		if(success) {
			return Optional.of(true);
		}
		return Optional.empty();
	}

}
