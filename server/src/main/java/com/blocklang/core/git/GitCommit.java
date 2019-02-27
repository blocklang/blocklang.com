package com.blocklang.core.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blocklang.core.git.exception.FileCreateOrUpdateFailedException;
import com.blocklang.core.git.exception.GitCommitFailedException;
import com.blocklang.core.git.exception.GitRepoNotFoundException;

public class GitCommit {

	private static final Logger logger = LoggerFactory.getLogger(GitCommit.class);
	
	/**
	 * git commit操作
	 * 
	 * @param gitRepoPath git仓库根目录
	 * @param relativePath 相对git仓库根目录的文件夹路径，支持使用 / 分割的路径
	 * @param fileName 文件名
	 * @param fileContent 内容
	 * @param authorName 作者名称
	 * @param authorMail 作者邮箱
	 * @param commitMessage 提交信息
	 * @return 返回 commit id
	 */
	public String execute(
			Path gitRepoPath, 
			String relativePath,
			String fileName,
			String fileContent, 
			String authorName, 
			String authorMail, 
			String commitMessage){
		File folder = gitRepoPath.resolve(Constants.DOT_GIT).toFile();
		try(Repository repo = FileRepositoryBuilder.create(folder);
				Git git = new Git(repo)){
			saveOrUpdateFile(gitRepoPath, relativePath, fileName, fileContent);
			// 注意 Filepattern 一定是相对仓库根目录的路径,如果是目录，则不能以/或\开头,分隔符只能是 /
			String filePattern = fileName;
			if(StringUtils.isNotBlank(relativePath)) {
				filePattern = relativePath + "/" + filePattern;
			}
			if(filePattern.startsWith("/")) {
				filePattern = filePattern.substring(1);
			}
			git.add().addFilepattern(filePattern).call();
			RevCommit commit = git.commit().setAuthor(authorName, authorMail).setMessage(commitMessage).call();
			return commit.getName();
		} catch (GitAPIException e) {
			throw new GitCommitFailedException(e);
		}catch (IOException e) {
			throw new GitRepoNotFoundException(gitRepoPath.toString(), e);
		}
	}

	private void saveOrUpdateFile(Path gitRepoPath, 
			String relativePath,
			String fileName, 
			String fileContent){
		try{
			Path folder = null;
			if(StringUtils.isBlank(relativePath)) {
				folder = gitRepoPath;
			}else {
				folder = gitRepoPath.resolve(Paths.get("", relativePath.split("/")));
			}
			
			if(Files.notExists(folder)){
				Files.createDirectories(folder);
			}
			Path file = folder.resolve(fileName);
			if(Files.notExists(file)){
				Files.createFile(file);
			}
			
			Files.writeString(file, fileContent);
		}catch(IOException e){
			throw new FileCreateOrUpdateFailedException(e);
		}
	}
	
	public RevCommit getLatestCommit(Path gitRepoPath) {
		File gitDir = gitRepoPath.resolve(Constants.DOT_GIT).toFile();
		String branch = Constants.MASTER;
		
		try (Repository repository = FileRepositoryBuilder.create(gitDir);
				RevWalk walk = new RevWalk(repository)) {
			Ref head = repository.findRef(branch);

			RevCommit commit = walk.parseCommit(head.getObjectId());
			walk.dispose();
			return commit;
		} catch (IOException e) {
			logger.error("获取最新提交信息失败", e);
		}
		return null;
	}

	public RevCommit getLatestCommit(Path gitRepoPath, String relativeFilePath) {
		File gitDir = gitRepoPath.resolve(Constants.DOT_GIT).toFile();
		String branch = Constants.MASTER;
		
		try (Repository repository = FileRepositoryBuilder.create(gitDir);
				RevWalk walk = new RevWalk(repository);
				Git git = new Git(repository)) {
			Ref head = repository.findRef(branch);
			
			if(StringUtils.isBlank(relativeFilePath)) {
				RevCommit commit = walk.parseCommit(head.getObjectId());
				walk.dispose();
				return commit;
			}
			
			ObjectId objectId = null;
			if(head == null){
				objectId = repository.resolve(branch);
			}else{
				objectId = head.getObjectId();
			}

			RevCommit commit = walk.parseCommit(objectId);
			Iterable<RevCommit> csIterable = git.log().add(commit.getId()).addPath(relativeFilePath).setMaxCount(1).call();
			return csIterable.iterator().next();
		} catch (IOException | GitAPIException e) {
			logger.error("获取最新提交信息失败", e);
		}
		return null;
	}
}
