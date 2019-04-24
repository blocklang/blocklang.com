package com.blocklang.core.git;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blocklang.core.constant.GitFileStatus;
import com.blocklang.core.git.exception.GitRepoNotFoundException;
import com.blocklang.core.git.exception.GitStatusFailedException;

public class GitStatus {
	
	private static final Logger logger = LoggerFactory.getLogger(GitStatus.class);
	
	private Path gitRepoPath;
	private String relativeDir;
	
	public GitStatus(Path gitRepoPath, String relativeDir) {
		this.gitRepoPath = gitRepoPath;
		this.relativeDir = relativeDir;
	}

	public Map<String, GitFileStatus> execute(){
		Map<String, GitFileStatus> result = new HashMap<String, GitFileStatus>();
		
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
			Git git = new Git(repo)){
			StatusCommand command = git.status();
			if(StringUtils.isNotBlank(relativeDir)) {
				command.addPath(relativeDir);
			}
			Status status = command.call();
			
			status.getUntracked().forEach(item -> result.put(item, GitFileStatus.UNTRACKED));
			status.getAdded().forEach(item -> result.put(item, GitFileStatus.ADDED));
			status.getModified().forEach(item -> result.put(item, GitFileStatus.MODIFIED));
			status.getMissing().forEach(item -> result.put(item, GitFileStatus.DELETED));
			status.getRemoved().forEach(item -> result.put(item, GitFileStatus.REMOVED));
			
			Set<String> a = status.getUncommittedChanges();
			System.out.println(a);
			
		} catch (NoWorkTreeException | GitAPIException e) {
			logger.error("git status 出错", e);
			throw new GitStatusFailedException(e);
		} catch (IOException e) {
			logger.error("仓库不存在", e);
			throw new GitRepoNotFoundException(gitRepoPath.toString());
		}
		
		return result;
		
	}
}
