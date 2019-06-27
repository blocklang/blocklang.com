package com.blocklang.marketplace.task;

import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.lib.Ref;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;

public class ApiRepoFindTagTask extends AbstractRepoPublishTask{
	
	private String version;
	
	public ApiRepoFindTagTask(MarketplacePublishContext marketplacePublishContext, String version) {
		super(marketplacePublishContext);
		this.version = version;
	}

	@Override
	public Optional<Ref> run() {
		try {
			List<Ref> tags = GitUtils.getTags(marketplacePublishContext.getLocalApiRepoPath().getRepoSourceDirectory());
			return tags.stream().filter(ref -> ref.getName().endsWith(this.version) || ref.getName().endsWith("v" + this.version)).findFirst();
		}catch (GitTagFailedException e) {
			logger.error(e);
			return Optional.empty();
		}
	}
}
