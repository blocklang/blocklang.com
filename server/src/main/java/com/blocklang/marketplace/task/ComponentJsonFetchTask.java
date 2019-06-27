package com.blocklang.marketplace.task;

import java.util.Optional;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;
import com.blocklang.marketplace.constant.MarketplaceConstant;

public class ComponentJsonFetchTask extends AbstractRepoPublishTask {

	private String gitRefName;

	public ComponentJsonFetchTask(MarketplacePublishContext context, String gitRefName) {
		super(context);
		this.gitRefName = gitRefName;
	}

	/**
	 * 获取 component.json 文件中的内容
	 */
	@Override
	public Optional<String> run() {
		try {
			return GitUtils.getBlob(context.getLocalComponentRepoPath().getRepoSourceDirectory(), gitRefName,
					MarketplaceConstant.FILE_NAME_COMPONENT).map(blobInfo -> blobInfo.getContent());
		} catch (GitTagFailedException e) {
			logger.error(e);
			return Optional.empty();
		}
	}
}