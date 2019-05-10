package com.blocklang.core.git;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.blocklang.core.git.exception.GitRepoNotFoundException;
import com.blocklang.core.git.exception.GitResetFailedException;

public class GitReset {
private static final Logger logger = LoggerFactory.getLogger(GitReset.class);
	
	private Path gitRepoPath;
	
	public GitReset(Path gitRepoPath) {
		this.gitRepoPath = gitRepoPath;
	}

	public void execute(String[] pathes) {
		Assert.notEmpty(pathes, "pathes 数组不能为空");
		
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
			Git git = new Git(repo)){
			ResetCommand command = git.reset();
			for(String path : pathes) {
				command.addPath(path);
			}
			command.call();
		} catch (IOException e) {
			logger.error("仓库不存在", e);
			throw new GitRepoNotFoundException(gitRepoPath.toString());
		} catch (GitAPIException e) {
			logger.error("执行 git reset 失败", e);
			throw new GitResetFailedException(e);
		} 
		
	}
}
