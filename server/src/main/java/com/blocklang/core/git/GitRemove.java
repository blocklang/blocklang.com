package com.blocklang.core.git;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.blocklang.core.git.exception.GitRemoveFailedException;
import com.blocklang.core.git.exception.GitRepoNotFoundException;

public class GitRemove {
	private static final Logger logger = LoggerFactory.getLogger(GitRemove.class);
	
	private Path gitRepoPath;
	
	public GitRemove(Path gitRepoPath) {
		this.gitRepoPath = gitRepoPath;
	}

	public void execute(String filePattern) {
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
			Git git = new Git(repo)){
			git.rm().addFilepattern(filePattern).call();
		} catch (IOException e) {
			logger.error("仓库不存在", e);
			throw new GitRepoNotFoundException(gitRepoPath.toString());
		} catch (GitAPIException e) {
			logger.error("执行 git remove 失败", e);
			throw new GitRemoveFailedException(e);
		} 
	}
	
	public void removeFromIndex(String[] filePatterns) {
		Assert.notEmpty(filePatterns, "filePatterns 数组不能为空");
		
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
			Git git = new Git(repo)){
			RmCommand command = git.rm().setCached(true);
			for(String filePattern : filePatterns) {
				command.addFilepattern(filePattern);
			}
			command.call();
		} catch (IOException e) {
			logger.error("仓库不存在", e);
			throw new GitRepoNotFoundException(gitRepoPath.toString());
		} catch (GitAPIException e) {
			logger.error("执行 git remove 失败", e);
			throw new GitRemoveFailedException(e);
		} 
		
	}

}
