package com.blocklang.release.task;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
			 Git git = new Git(repo)){
			git.tag().setName(appBuildContext.getTagName()).call();
			return true;
		} catch (IOException | GitAPIException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}
}
