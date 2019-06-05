package com.blocklang.marketplace.task;

import java.nio.file.Path;
import java.util.Optional;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitCloneFailedException;
import com.blocklang.core.git.exception.GitPullFailedException;
import com.blocklang.core.git.exception.GitRepoNotFoundException;

public class GitSyncComponentRepoTask extends AbstractRepoPublishTask{

	public GitSyncComponentRepoTask(MarketplacePublishContext marketplacePublishContext) {
		super(marketplacePublishContext);
	}

	@Override
	public Optional<Boolean> run() {
		try {
			Path path = marketplacePublishContext.getRepoSourceDirectory();
			if(GitUtils.isGitRepo(path)) {
				logger.info("从 {0} 仓库拉取最新的组件仓库源码", marketplacePublishContext.getGitUrl());
				GitUtils.pull(path);
			} else {
				logger.info("从 {0} 仓库克隆组件仓库源码", marketplacePublishContext.getGitUrl());
				GitUtils.clone(marketplacePublishContext.getGitUrl(), path);
			}
			return Optional.of(true);
		} catch (GitRepoNotFoundException | GitPullFailedException | GitCloneFailedException e) {
			logger.error(e);
			return Optional.empty();
		}
	}
	
}
