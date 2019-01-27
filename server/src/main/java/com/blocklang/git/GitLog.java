package com.blocklang.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.blocklang.git.exception.GitLogFailedException;
import com.blocklang.git.exception.GitNoHeadException;
import com.blocklang.git.exception.GitRepoNotFoundException;

public class GitLog {

	public int getCount(Path gitRootFolder){
		File gitDir = gitRootFolder.resolve(Constants.DOT_GIT).toFile();
		try (Repository repo = FileRepositoryBuilder.create(gitDir);
				Git git = new Git(repo)){
			
			LogCommand logCommand = git.log();
			logCommand.all();
			Iterable<RevCommit> commits = logCommand.call();
			Iterator<RevCommit> iterator = commits.iterator();
			
			int count = 0;
			while(iterator.hasNext()){
				count++;
				iterator.next();
			}
			return count;
		} catch (IOException e) {
			throw new GitRepoNotFoundException(gitRootFolder.toString(), e);
		} catch (NoHeadException e) {
			throw new GitNoHeadException(e);
		} catch (GitAPIException e) {
			throw new GitLogFailedException(e);
		}
	}

}

