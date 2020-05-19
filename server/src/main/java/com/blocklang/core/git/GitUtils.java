package com.blocklang.core.git;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.FileUtils;

import com.blocklang.core.constant.GitFileStatus;

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
	
	public static String commit(
			Path gitRepoPath, 
			String authorName, 
			String authorMail,
			String commitMessage) {
		GitCommit gitCommit = new GitCommit();
		return gitCommit.execute(gitRepoPath, authorName, authorMail, commitMessage);
	}
	
	/**
	 * git pull
	 * 
	 * @param gitRepoPath 仓库的根目录
	 */
	public static void pull(Path gitRepoPath){
		GitPull gitPull = new GitPull();
		gitPull.execute(gitRepoPath, false);
	}
	
	public static void pullWithTag(Path gitRepoPath) {
		GitPull gitPull = new GitPull();
		gitPull.execute(gitRepoPath, true);
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
	
	public static Optional<Ref> getLatestTag(Path gitRepoPath) {
		GitTag gitTag = new GitTag();
		return gitTag.getLatestTag(gitRepoPath);
	}
	
	public static List<Ref> getTags(Path gitRepoPath) {
		GitTag gitTag = new GitTag();
		return gitTag.getTags(gitRepoPath);
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
	 * 从 repository 往本地的 localFolderPath 同步 git 仓库。
	 * 
	 * 如果本地不存在仓库，则执行 git clone，否则执行 git pull。
	 * 
	 * @param repository
	 * @param localFolderPath
	 */
	public static void syncRepository(String repository, Path localFolderPath) {
		if(GitUtils.isGitRepo(localFolderPath)) { 
			// 本地已存在仓库，同步
			GitUtils.pullWithTag(localFolderPath);
		} else { 
			// 本地不存在仓库，克隆
			GitUtils.clone(repository, localFolderPath);
		}
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
	
	/**
	 * 
	 * @param gitRepoPath
	 * @param relativeDir 传入 null 表示根目录
	 * @return
	 */
	public static List<GitFileInfo> getFiles(Path gitRepoPath, String relativeDir) {
		GitFile file = new GitFile(gitRepoPath, relativeDir);
		return file.execute();
	}
	
	/**
	 * 从指定的分支或 tag 下获取文件路径信息，不包括文件夹，但递归查找
	 * 
	 * @param gitRepoPath
	 * @param refName 需要包含 refs/tags/ 前缀
	 * @param pathSuffix 如果只查出 json 文件，则值为".json"；如果值为 null，显示全部
	 * @return
	 */
	public static List<GitFileInfo> readAllFiles(Path gitRepoPath, String refName, String pathSuffix) {
		GitFile file = new GitFile(gitRepoPath, null);
		return file.readAllFiles(refName, pathSuffix);
	}
	
	/**
	 * 
	 * @param gitRepoPath
	 * @param ref 可以是分支或 tag。注意，不能使用简称，如不能为“master”，而应该为“refs/heads/master”；不能为“v0.1.0”，应该为“refs/tags/v0.1.0”
	 * @param filePath
	 * @return
	 */
	public static Optional<GitBlobInfo> getBlob(Path gitRepoPath, String ref, String filePath) {
		GitBlob blob = new GitBlob(gitRepoPath, ref, filePath);
		return blob.execute();
	}
	
	public static List<GitBlobInfo> loadDataFromTag(Path gitRepoPath, String refName, List<GitFileInfo> files) {
		GitBlob blob = new GitBlob(gitRepoPath, refName);
		return blob.loadDataFromTag(files);
	}
	
	/**
	 * 
	 * 
	 * @param gitRepoPath
	 * @param relativeDir 传入 null 表示根目录
	 * @return
	 */
	public static Map<String, GitFileStatus> status(Path gitRepoPath, String relativeDir){
		GitStatus status = new GitStatus(gitRepoPath, relativeDir);
		return status.execute();
	}

	/**
	 * 
	 * @param gitRepoPath
	 * @param filePattern 不能以 / 或 \ 开头，只能以 / 分割；. 表示全部
	 */
	public static void add(Path gitRepoPath, String filePattern) {
		GitAdd add = new GitAdd(gitRepoPath);
		add.execute(filePattern);
	}
	
	public static void add(Path gitRepoPath, String[] filePatterns) {
		GitAdd add = new GitAdd(gitRepoPath);
		add.execute(filePatterns);
	}

	/**
	 * 
	 * @param gitRepoPath
	 * @param filePattern 不能以 / 或 \ 开头，只能以 / 分割；. 表示全部
	 */
	public static void remove(Path gitRepoPath, String filePattern) {
		GitRemove remove = new GitRemove(gitRepoPath);
		remove.execute(filePattern);
	}
	
	public static void removeFromIndex(Path gitRepoPath, String[] filePatterns) {
		GitRemove remove = new GitRemove(gitRepoPath);
		remove.removeFromIndex(filePatterns);
	}
	
	public static void reset(Path gitRepoPath, String[] pathes){
		GitReset reset = new GitReset(gitRepoPath);
		reset.execute(pathes);
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

	public static boolean isValidRemoteRepository(String remoteGitUrl) {
		if(StringUtils.isBlank(remoteGitUrl)) {
			return false;
		}
		
		URIish uriish = null;
		try {
			uriish = new URIish(remoteGitUrl);
		} catch (URISyntaxException e) {
			return false;
		}
		
		if(!uriish.isRemote()) {
			return false;
		}

		try {
			return !Git.lsRemoteRepository().setHeads(false).setTags(false).setRemote(remoteGitUrl).call().isEmpty();
		} catch (GitAPIException e) {
			return false;
		}
	}
	
	public static Optional<String> getTagName(String refName) {
		if(StringUtils.isBlank(refName)) {
			return Optional.empty();
		}
		
		String stripedRefName = refName.strip();
		if(stripedRefName.startsWith(Constants.R_TAGS)) {
			stripedRefName = stripedRefName.substring(Constants.R_TAGS.length());
		}
		
		if(StringUtils.isBlank(stripedRefName)) {
			return Optional.empty();
		}
		
		return Optional.of(stripedRefName);
	}

	public static Optional<String> getVersionFromRefName(String refName) {
		if(StringUtils.isBlank(refName)) {
			return Optional.empty();
		}
		
		String stripedRefName = refName.strip();
		if(stripedRefName.startsWith(Constants.R_TAGS)) {
			stripedRefName = stripedRefName.substring(Constants.R_TAGS.length());
		}
		if(stripedRefName.toLowerCase().startsWith("v")) {
			stripedRefName = stripedRefName.substring(1);
		}
		
		if(StringUtils.isBlank(stripedRefName)) {
			return Optional.empty();
		}
		
		return Optional.of(stripedRefName);
	}

	public static void checkout(Path gitRepoPath, String branchOrTagName) {
		GitCheckout checkout = new GitCheckout();
		checkout.execute(gitRepoPath, branchOrTagName);
	}

	public static String getCurrentBranch(Path gitRepoPath) throws IOException {
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repo = FileRepositoryBuilder.create(gitDir.toFile())){
			return repo.getBranch();
		}
	}
	
}
