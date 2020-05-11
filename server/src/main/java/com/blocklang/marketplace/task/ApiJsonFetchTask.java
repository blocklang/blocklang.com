package com.blocklang.marketplace.task;

import java.util.Optional;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;
import com.blocklang.core.runner.common.AbstractTask;
import com.blocklang.core.runner.common.CliContext;
import com.blocklang.marketplace.constant.MarketplaceConstant;

public class ApiJsonFetchTask extends AbstractPublishRepoTask {

	private String gitRefName;

	public ApiJsonFetchTask(CliContext<MarketplacePublishData> context, String gitRefName) {
		super(context);
		this.gitRefName = gitRefName;
	}

	/**
	 * 获取 api.json 文件中的内容
	 */
	@Override
	public Optional<String> run() {
		try {
			return GitUtils.getBlob(data.getLocalApiRepoPath().getRepoSourceDirectory(), gitRefName,
					MarketplaceConstant.FILE_NAME_API).map(blobInfo -> blobInfo.getContent());
		} catch (GitTagFailedException e) {
			logger.error(e);
			return Optional.empty();
		}
	}
}