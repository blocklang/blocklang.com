package com.blocklang.marketplace.task;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Ref;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;
import com.blocklang.core.runner.common.CliContext;

import de.skuzzle.semantic.Version;

/**
 * 获取 API 仓库的所有正式版本号。
 * 
 * 先获取 API 仓库的所有 tag，然后解析出其中的版本号，并做校验，判断是不是有效的语义化版本，且不是预发布版本号。
 * 
 * 
 * @author Zhengwei Jin
 *
 */
public class ApiRepoVersionsFindTask extends AbstractPublishRepoTask{
	
	public ApiRepoVersionsFindTask(CliContext<MarketplacePublishData> marketplacePublishContext) {
		super(marketplacePublishContext);
	}

	@Override
	public Optional<Boolean> run() {
		try {
			List<Ref> tags = GitUtils.getTags(data.getLocalApiRepoPath().getRepoSourceDirectory());
			logger.info("共有 {0} 个 git tags", tags.size());
			
			data.setAllApiRepoTagNames(tags.stream().map(ref -> ref.getName()).collect(Collectors.toList()));
			
			List<String> versions = tags.stream()
				.map(ref -> GitUtils.getVersionFromRefName(ref.getName()))
				.flatMap(Optional::stream)
				.filter(version -> {
					if(!Version.isValidVersion(version)) {
						logger.info("过滤掉无效的版本号 {0}", version);
						return false;
					}
					return true;
				})
				.collect(Collectors.toList());
			
			if(versions.isEmpty()) {
				logger.error("共解析出 0 个有效的版本");
				return Optional.empty();
			}
			data.setApiRepoVersions(versions);
			return Optional.of(true);
		}catch (GitTagFailedException e) {
			logger.error(e);
			return Optional.empty();
		}
	}
}
