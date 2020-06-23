package com.blocklang.core.git;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.blocklang.core.git.exception.GitTagFailedException;

public class GitDescribe {

	public String execute(Path gitRepoPath) {
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
			 Git git = new Git(repo)){
			return git.describe().call();
		} catch (IOException | GitAPIException e) {
			throw new GitTagFailedException(e);
		}
	}
	
}
