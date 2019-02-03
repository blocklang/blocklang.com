package com.blocklang.core.git;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.blocklang.core.git.exception.GitTagFailedException;

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

	public Ref tag(Path gitRepoPath, String tagName, String message) {
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
			 Git git = new Git(repo)){
			return git.tag().setName(tagName).setMessage(message).call();
		} catch (IOException | GitAPIException e) {
			throw new GitTagFailedException(e);
		}
	}
	
	public RevCommit tagThenReturnCommit(Path gitRepoPath, String tagName, String message) {
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile());
			 Git git = new Git(repo);
			 RevWalk walk = new RevWalk(repo)){
			Ref ref =  git.tag().setName(tagName).setMessage(message).call();
			return walk.parseCommit(ref.getObjectId());
		} catch (IOException | GitAPIException e) {
			throw new GitTagFailedException(e);
		}
	}

	public Optional<Ref> getTag(Path gitRepoPath, String tagName) {
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile())) {
			Ref tag = repo.findRef(tagName);
			return Optional.ofNullable(tag);
		} catch (IOException e) {
			throw new GitTagFailedException("get tag by tag name failed", e);
		}
	}
}
