package com.blocklang.git;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.blocklang.git.exception.GitTagFailedException;

public class GitTag {

	public int getCount(Path gitRepoPath) {
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
			 Git git = new Git(repo)){
			return git.tagList().call().size();
		} catch (IOException | GitAPIException e) {
			throw new GitTagFailedException(e);
		}
	}

	public void tag(Path gitRepoPath, String tagName, String message) {
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
			 Git git = new Git(repo)){
			git.tag().setName(tagName).setMessage(message).call();
		} catch (IOException | GitAPIException e) {
			throw new GitTagFailedException(e);
		}
	}

}
