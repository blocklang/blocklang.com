package com.blocklang.marketplace.task;

import java.util.Optional;

import org.eclipse.jgit.lib.Ref;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;

public class LatestGitTagFetchTask extends AbstractRepoPublishTask{

	public LatestGitTagFetchTask(MarketplacePublishContext marketplacePublishContext) {
		super(marketplacePublishContext);
	}

	@Override
	public Optional<Ref> run() {
		try {
			return GitUtils.getLatestTag(marketplacePublishContext.getRepoSourceDirectory());
		}catch (GitTagFailedException e) {
			logger.error(e);
			return Optional.empty();
		}
		
	}
}
