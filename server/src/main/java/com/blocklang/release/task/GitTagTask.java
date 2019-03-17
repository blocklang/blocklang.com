package com.blocklang.release.task;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.jgit.lib.Ref;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;

public class GitTagTask extends AbstractTask{

	private Path gitDir;
	
	public GitTagTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
		// 获取已存在的 git 仓库
		gitDir = appBuildContext.getGitRepositoryDirectory();
	}

	// 为 git 仓库打标签
	@Override
	public Optional<String> run() {
		try {
			Ref tag = GitUtils.tag(gitDir, appBuildContext.getTagName(), "");
			return Optional.of(tag.getObjectId().getName());
		} catch (GitTagFailedException e) {
			appBuildContext.error(e);
			return Optional.empty();
		}
	}
	
	public boolean exists() {
		try {
			return GitUtils.getTag(gitDir, appBuildContext.getTagName()).map((tag) -> {return true;}).orElse(false);
		} catch (GitTagFailedException e) {
			appBuildContext.error(e);
			return false;
		}
	}
}
