package com.blocklang.marketplace.task;

import java.util.Optional;

import org.eclipse.jgit.lib.Ref;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;

public class ComponentRepoLatestTagFetchTask extends AbstractRepoPublishTask{

	public ComponentRepoLatestTagFetchTask(MarketplacePublishContext context) {
		super(context);
	}

	@Override
	public Optional<Ref> run() {
		try {
			return GitUtils.getLatestTag(context.getLocalComponentRepoPath().getRepoSourceDirectory());
		}catch (GitTagFailedException e) {
			logger.error(e);
			return Optional.empty();
		}
	}
}
