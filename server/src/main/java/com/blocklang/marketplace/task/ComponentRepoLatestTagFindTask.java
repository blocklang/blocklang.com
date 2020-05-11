package com.blocklang.marketplace.task;

import java.util.Optional;

import org.eclipse.jgit.lib.Ref;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;
import com.blocklang.core.runner.CliContext;

public class ComponentRepoLatestTagFindTask extends AbstractPublishRepoTask{

	public ComponentRepoLatestTagFindTask(CliContext<MarketplacePublishData> context) {
		super(context);
	}

	@Override
	public Optional<Ref> run() {
		try {
			return GitUtils.getLatestTag(data.getLocalComponentRepoPath().getRepoSourceDirectory());
		}catch (GitTagFailedException e) {
			logger.error(e);
			return Optional.empty();
		}
	}
}
