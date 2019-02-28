package com.blocklang.core.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.FileUtils;

/**
 * git 帮助类，本工具类适合每次对 git 仓库做一个操作。
 * 如果需要连续为仓库做多个操作，可考虑在这里添加常用的组合。
 * 因为每个操作都需要做初始化 Repository 的操作，所以当需要连续多个操作时，性能并不是最优的。
 * 
 * @author Zhengwei Jin
 */
public class GitUtils {

	/**
	 * 判断指定路径对应的文件夹是不是git仓库的根目录。
	 * 
	 * @param gitRepoPath 文件夹路径
	 * @return 如果文件夹下是git仓库，则返回<code>true</code>;否则返回<code>false</code>
	 */
	public static boolean isGitRepo(Path gitRootFolder) {
		if(Files.notExists(gitRootFolder)) {
			return false;
		}
		if(!Files.isDirectory(gitRootFolder)) {
			return false;
		}

		return RepositoryCache.FileKey.isGitRepository(gitRootFolder.resolve(Constants.DOT_GIT).toFile(), FS.DETECTED);
	}
	
	public static int getLogCount(Path gitRootFolder){
		GitLog gitLog = new GitLog();
		return gitLog.getCount(gitRootFolder);
	}

	/**
	 * 初始化git仓库
	 * 
	 * @param gitRepoPath 文件夹路径
	 * @param gitUserName 用户名
	 * @param gitUserMail 邮箱
	 */
	public static String init(Path gitRepoPath, String gitUserName, String gitUserMail){
		GitInit gitInit = new GitInit();
		return gitInit.execute(gitRepoPath, gitUserName, gitUserMail);
	}
	
	public static GitInit beginInit(Path gitRepoPath, String gitUserName, String gitUserMail) {
		GitInit gitInit = new GitInit(gitRepoPath, gitUserName, gitUserMail);
		return gitInit;
	}
	
	/**
	 * 
	 * @param gitRootPath
	 * @param relativePath 项目仓库根目录的路径，不要以 “/” 开头
	 * @param fileName
	 * @param fileContent
	 * @param authorName
	 * @param authorMail
	 * @param commitMessage
	 * @return
	 */
	public static String commit(
			Path gitRootPath, 
			String relativePath,
			String fileName,
			String fileContent, 
			String authorName, 
			String authorMail, 
			String commitMessage){
		GitCommit gitCommit = new GitCommit();
		return gitCommit.execute(gitRootPath, relativePath, fileName, fileContent, authorName, authorMail, commitMessage);
	}
	
	/**
	 * git pull
	 * 
	 * @param gitRepoPath 仓库的根目录
	 */
	public static void pull(Path gitRepoPath){
		GitPull gitPull = new GitPull();
		gitPull.execute(gitRepoPath);
	}

	/**
	 * 删除git仓库
	 * 
	 * @param gitRepoPath 文件夹路径
	 * @throws IOException 
	 */
	public static void delete(String gitRepoPath) throws IOException {
		File file = new File(gitRepoPath);
		FileUtils.delete(file, FileUtils.RECURSIVE);
	}

	public static int getTagCount(Path gitRepoPath) {
		GitTag gitTag = new GitTag();
		return gitTag.getCount(gitRepoPath);
	}

	public static Ref tag(Path gitRepoPath, String tagName, String message) {
		GitTag gitTag = new GitTag();
		return gitTag.tag(gitRepoPath, tagName, message);
	}
	
	public static String tagThenReturnCommitId(Path gitRepoPath, String tagName, String message) {
		GitTag gitTag = new GitTag();
		return gitTag.tagThenReturnCommit(gitRepoPath, tagName, message).getName();
	}

	public static Optional<Ref> getTag(Path gitRepoPath, String tagName) {
		GitTag gitTag = new GitTag();
		return gitTag.getTag(gitRepoPath, tagName);
	}
	
	/**
	 * 从远程 git 仓库克隆项目
	 * 
	 * @param remoteGitUrl
	 * @param localFolderPath
	 */
	public static void clone(String remoteGitUrl, Path localFolderPath) {
		GitClone gitClone = new GitClone();
		gitClone.execute(remoteGitUrl, localFolderPath);
	}
	
	/**
	 * 获取 master 分支根目录上的最近一次提交
	 * 
	 * @param gitRepoPath
	 * @return 提交信息
	 */
	public static RevCommit getLatestCommit(Path gitRepoPath) {
		GitCommit commit = new GitCommit();
		return commit.getLatestCommit(gitRepoPath);
	}

	/**
	 * 获取 master 分支指定目录上的最近一次提交
	 * 
	 * @param gitRepoPath
	 * @return 提交信息
	 */
	public static RevCommit getLatestCommit(Path gitRepoPath, String relativeFilePath) {
		GitCommit commit = new GitCommit();
		return commit.getLatestCommit(gitRepoPath, relativeFilePath);
	}
	
	public static List<GitFileInfo> getFiles(Path gitRepoPath, String relativeDir) {
		GitFile file = new GitFile(gitRepoPath, relativeDir);
		return file.execute();
	}
	
	// 暂时不要删除此代码
	// 用于本地测试 clone 和 pull 方法
	public static void main(String[] args) {
		Path path = Paths.get("E:\\data\\blocklang\\template");
		if(isGitRepo(path)) {
			pull(path);
		} else {
			clone("https://github.com/blocklang/blocklang-template.git", path);
		}
	}

}
