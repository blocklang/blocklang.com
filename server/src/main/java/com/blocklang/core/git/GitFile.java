package com.blocklang.core.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.blocklang.core.git.exception.GitFileNotFoundException;
import com.blocklang.core.util.DateUtil;
import com.nimbusds.oauth2.sdk.util.StringUtils;

public class GitFile {

	private static final Logger logger = LoggerFactory.getLogger(GitFile.class);
	
	private Path gitRepoPath;
	private String relativeDir;
	
	public GitFile(Path gitRepoPath, String relativeDir) {
		this.gitRepoPath = gitRepoPath;
		this.relativeDir = relativeDir;
	}

	public List<GitFileInfo> execute() {
		List<GitFileInfo> files = new ArrayList<GitFileInfo>();
		File gitDir = gitRepoPath.resolve(Constants.DOT_GIT).toFile();
		if(!gitDir.exists()) {
			return files;
		}
		
		String branch = Constants.R_HEADS + Constants.MASTER;
		
		try(Repository repository = FileRepositoryBuilder.create(gitDir);
				Git git = new Git(repository);
				RevWalk walk = new RevWalk(repository)){
			
			Ref head = repository.exactRef(branch);
			ObjectId objectId = head.getObjectId();
			RevCommit commit = walk.parseCommit(objectId);
			RevTree tree = commit.getTree();
			
			if(StringUtils.isBlank(relativeDir)) {
				try(TreeWalk treeWalk = new TreeWalk(repository)) {
					treeWalk.addTree(tree);
					treeWalk.setRecursive(false);
					treeWalk.setPostOrderTraversal(false);
					while (treeWalk.next()) {
						GitFileInfo fileInfo = new GitFileInfo();
						fileInfo.setPath(treeWalk.getPathString());
						fileInfo.setName(treeWalk.getNameString());
						fileInfo.setFolder(treeWalk.isSubtree());

						Iterable<RevCommit> latestLogs = git.log().addPath(treeWalk.getPathString()).setMaxCount(1).call();
						RevCommit latestCommit = latestLogs.iterator().next();
						
						fileInfo.setCommitId(latestCommit.getName());
						fileInfo.setLatestShortMessage(latestCommit.getShortMessage());
						fileInfo.setLatestFullMessage(latestCommit.getFullMessage());
						fileInfo.setLatestCommitTime(DateUtil.ofSecond(latestCommit.getCommitTime()));
						
						files.add(fileInfo);
					}
				} catch (NoHeadException e) {
					logger.error(e.getMessage(), e);
				} catch (GitAPIException e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				try(TreeWalk treeWalk = buildTreeWalk(repository, tree, relativeDir)) {
					if((treeWalk.getFileMode(0).getBits() & FileMode.TYPE_TREE) == 0) {
						throw new IllegalStateException("Tried to read the elements of a non-tree for commit '"
								+ commit + "' and path '" 
								+ relativeDir + "', had filemode " 
								+ treeWalk.getFileMode(0).getBits());
					}
					try (TreeWalk dirWalk = new TreeWalk(repository);) {
						dirWalk.addTree(treeWalk.getObjectId(0));
						dirWalk.setRecursive(false);
						while(dirWalk.next()) {
							String path = treeWalk.getPathString() + "/" + dirWalk.getPathString();
							
							GitFileInfo fileInfo = new GitFileInfo();
							fileInfo.setPath(path);
							fileInfo.setName(dirWalk.getNameString());
							fileInfo.setFolder(dirWalk.isSubtree());
							
							Iterable<RevCommit> latestLogs = git.log().add(commit.getId())
									.addPath(path)
									.setMaxCount(1)
									.call();
							
							RevCommit latestCommit = latestLogs.iterator().next();
							fileInfo.setCommitId(latestCommit.getName());
							fileInfo.setLatestShortMessage(latestCommit.getShortMessage());
							fileInfo.setLatestFullMessage(latestCommit.getFullMessage());
							fileInfo.setLatestCommitTime(DateUtil.ofSecond(latestCommit.getCommitTime()));
							
							files.add(fileInfo);
						}
					} catch (NoHeadException e) {
						logger.error(e.getMessage(), e);
					} catch (GitAPIException e) {
						logger.error(e.getMessage(), e);
					}
				} catch (GitFileNotFoundException e) {
					logger.error(e.getMessage(), e);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		return files;
	}
	
	private static TreeWalk buildTreeWalk(Repository repository, RevTree tree, final String path) throws IOException {
		TreeWalk treeWalk = TreeWalk.forPath(repository, path, tree);
		if(treeWalk == null) {
			 throw new GitFileNotFoundException("Did not find expected file '" + path + "' in tree '" + tree.getName() + "'");
		}
		return treeWalk;
	}

	public List<GitFileInfo> getAllFilesFromTag(String refName, String pathSuffix) {
		Assert.hasText(refName, "tag 的值不能为空");
		File gitDir = gitRepoPath.resolve(Constants.DOT_GIT).toFile();
		if(!gitDir.exists()) {
			return Collections.emptyList();
		}
		
		try(Repository repository = FileRepositoryBuilder.create(gitDir);
				Git git = new Git(repository);
				RevWalk walk = new RevWalk(repository)){
			
			Ref tag = repository.exactRef(refName);
			if(tag == null) {
				return Collections.emptyList();
			}
			ObjectId objectId = tag.getObjectId();
			RevCommit commit = walk.parseCommit(objectId);
			RevTree tree = commit.getTree();
			
			List<GitFileInfo> files = new ArrayList<GitFileInfo>();
			
			try(TreeWalk treeWalk = new TreeWalk(repository)) {
				treeWalk.addTree(tree);
				treeWalk.setRecursive(true);
				if(StringUtils.isNotBlank(pathSuffix)) {
					treeWalk.setFilter(PathSuffixFilter.create(pathSuffix));
				}
				while (treeWalk.next()) {
					GitFileInfo fileInfo = new GitFileInfo();
					fileInfo.setPath(treeWalk.getPathString());
					fileInfo.setName(treeWalk.getNameString());
					fileInfo.setFolder(treeWalk.isSubtree());

					files.add(fileInfo);
				}
			}
			return files;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return Collections.emptyList();
	}

}
