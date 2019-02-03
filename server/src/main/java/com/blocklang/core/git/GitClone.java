package com.blocklang.core.git;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blocklang.core.git.exception.GitCloneFailedException;

public class GitClone {

	private static final Logger logger = LoggerFactory.getLogger(GitClone.class);
	
	public void execute(String remoteGitUrl, Path localFolderPath) {
		if(Files.exists(localFolderPath) && localFolderPath.toFile().listFiles().length > 0) {
			logger.error("{} 已存在且目录不为空，无法执行 git clone 操作", localFolderPath);
			throw new GitCloneFailedException(localFolderPath.toString() + " 目录中已存在内容，克隆失败");
		}
		
		try (Git result = Git.cloneRepository()
				.setURI(remoteGitUrl)
				.setProgressMonitor(new GitCloneProgressMonitor())
				.setDirectory(localFolderPath.toFile())
				.call()) {
			logger.info("Clone 完成: {} 成功克隆到 {}", remoteGitUrl, localFolderPath);
		} catch (InvalidRemoteException e) {
			logger.error("克隆失败", e);
			throw new GitCloneFailedException(e);
		} catch (TransportException e) {
			logger.error("克隆失败", e);
			throw new GitCloneFailedException(e);
		} catch (GitAPIException e) {
			logger.error("克隆失败", e);
			throw new GitCloneFailedException(e);
		}
	}

}
