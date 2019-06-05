package com.blocklang.marketplace.task;

import java.util.Optional;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;

public class BlockLangJsonFetchTask extends AbstractRepoPublishTask{

	private String ref;
	
	public BlockLangJsonFetchTask(MarketplacePublishContext marketplacePublishContext, String ref) {
		super(marketplacePublishContext);
		this.ref = ref;
	}

	/**
	 * 获取 blocklang.json 文件中的内容
	 */
	@Override
	public Optional<String> run() {
		try {
			return GitUtils.getBlob(marketplacePublishContext.getRepoSourceDirectory(), ref, "blocklang.json").map(blobInfo -> blobInfo.getContent());
		}catch (GitTagFailedException e) {
			logger.error(e);
			return Optional.empty();
		}
	}
}