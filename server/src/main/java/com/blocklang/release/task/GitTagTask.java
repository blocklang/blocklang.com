package com.blocklang.release.task;

import java.nio.file.Path;

import org.eclipse.jgit.lib.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blocklang.git.GitUtils;
import com.blocklang.git.exception.GitTagFailedException;

public class GitTagTask extends AbstractTask{

	private Logger logger = LoggerFactory.getLogger(GitTagTask.class);
	
	public GitTagTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}

	// 为 git 仓库打标签
	@Override
	public boolean run() {
		// 获取已存在的 git 仓库
		Path gitDir = appBuildContext.getGitRepositoryDirectory().resolve(Constants.DOT_GIT);
		try {
			GitUtils.tagThenReturnCommitId(gitDir, appBuildContext.getTagName(), "");
			return true;
		} catch (GitTagFailedException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}
}
