package com.blocklang.core.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.blocklang.core.git.exception.GitFileNotFoundException;
import com.blocklang.core.util.DateUtil;

public class GitBlob {
	private static final Logger logger = LoggerFactory.getLogger(GitBlob.class);
	
	private Path gitRepoPath;
	private String ref; // branch/tag
	private String filePath;
	
	public GitBlob(Path gitRepoPath, String ref) {
		this.gitRepoPath = gitRepoPath;
		this.ref = ref;
	}
	
	public GitBlob(Path gitRepoPath, String ref, String filePath) {
		this(gitRepoPath, ref);
		this.filePath = filePath;
	}

	public Optional<GitBlobInfo> execute() {
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		
		try (Repository repository = FileRepositoryBuilder.create(gitDir.toFile());
				Git git = new Git(repository);
				RevWalk walk = new RevWalk(repository)) {
			Ref ref = repository.exactRef(this.ref);
			if(ref == null) {
				return Optional.empty();
			}
			
			ObjectId objectId = ref.getObjectId();;

			RevCommit commit = walk.parseCommit(objectId);
			RevTree tree = commit.getTree();
			try (TreeWalk treeWalk = buildTreeWalk(repository, tree, filePath)) {

				if ((treeWalk.getFileMode(0).getBits() & FileMode.TYPE_FILE) == 0) {
					throw new IllegalStateException("Tried to read the elements of a non-tree for commit '" + commit
							+ "' and path '" + filePath + "', had filemode " + treeWalk.getFileMode(0).getBits());
				}
				
				GitBlobInfo blobInfo = new GitBlobInfo();
				blobInfo.setPath(treeWalk.getPathString());
				blobInfo.setName(treeWalk.getNameString());

				// 文件内容
				ObjectId blobObjectId = treeWalk.getObjectId(0);
				ObjectLoader loader = repository.open(blobObjectId);
				blobInfo.setContent(new String(loader.getBytes()));

				Iterable<RevCommit> latestLogs = git.log().add(commit.getId()).addPath(treeWalk.getPathString()).setMaxCount(1).call();
				RevCommit latestCommit = latestLogs.iterator().next();
				blobInfo.setCommitId(latestCommit.getName());
				blobInfo.setLatestShortMessage(latestCommit.getShortMessage());
				blobInfo.setLatestFullMessage(latestCommit.getFullMessage());
				blobInfo.setLatestCommitTime(DateUtil.ofSecond(latestCommit.getCommitTime()));
				
				return Optional.of(blobInfo);
			} catch (NoHeadException e) {
				logger.error(e.getMessage(), e);
			} catch (GitAPIException e) {
				logger.error(e.getMessage(), e);
			} catch (MissingObjectException e) {
				logger.error(e.getMessage(), e);
			} catch (GitFileNotFoundException e) {
				logger.error(e.getMessage(), e);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		return Optional.empty();
	}
	
	private static TreeWalk buildTreeWalk(Repository repository, RevTree tree, final String path) throws IOException {
		TreeWalk treeWalk = TreeWalk.forPath(repository, path, tree);
		if(treeWalk == null) {
			 throw new GitFileNotFoundException("Did not find expected file '" + path + "' in tree '" + tree.getName() + "'");
		}
		return treeWalk;
	}

	public List<GitBlobInfo> loadDataFromTag(List<GitFileInfo> files) {
		Assert.notNull(files, "传入的值不能为null");
		if(files.isEmpty()) {
			return Collections.emptyList();
		}
		
		Path gitDir = gitRepoPath.resolve(Constants.DOT_GIT);
		try (Repository repository = FileRepositoryBuilder.create(gitDir.toFile());
				Git git = new Git(repository);
				RevWalk walk = new RevWalk(repository)) {
			Ref ref = repository.exactRef(this.ref);
			if(ref == null) {
				return Collections.emptyList();
			}
			
			ObjectId objectId = ref.getObjectId();;

			RevCommit commit = walk.parseCommit(objectId);
			RevTree tree = commit.getTree();
		
			return files.stream().map(gitFileInfo -> {
				try (TreeWalk treeWalk = buildTreeWalk(repository, tree, gitFileInfo.getPath())) {

					if ((treeWalk.getFileMode(0).getBits() & FileMode.TYPE_FILE) == 0) {
						throw new IllegalStateException("Tried to read the elements of a non-tree for commit '" + commit
								+ "' and path '" + filePath + "', had filemode " + treeWalk.getFileMode(0).getBits());
					}
					
					GitBlobInfo blobInfo = new GitBlobInfo();
					blobInfo.setPath(treeWalk.getPathString());
					blobInfo.setName(treeWalk.getNameString());
					blobInfo.setFolder(false);

					// 文件内容
					ObjectId blobObjectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(blobObjectId);
					blobInfo.setContent(new String(loader.getBytes()));

					return blobInfo;
				} catch (MissingObjectException e) {
					logger.error(e.getMessage(), e);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				return null;
			}).collect(Collectors.toList());
		}catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return Collections.emptyList();
	}


	public List<GitBlobInfo> readAllFiles(TreeFilter treeFilter) {
		Assert.hasText(this.ref, "refName 的值不能为空");
		File gitDir = gitRepoPath.resolve(Constants.DOT_GIT).toFile();
		if(!gitDir.exists()) {
			return Collections.emptyList();
		}
		
		try(Repository repository = FileRepositoryBuilder.create(gitDir);
				Git git = new Git(repository);
				RevWalk walk = new RevWalk(repository)){
			Ref ref = repository.exactRef(this.ref);
			if(ref == null) {
				return Collections.emptyList();
			}
			
			ObjectId objectId = ref.getObjectId();
			RevCommit commit = walk.parseCommit(objectId);
			RevTree tree = commit.getTree();
			
			List<GitBlobInfo> files = new ArrayList<GitBlobInfo>();
			
			try(TreeWalk treeWalk = new TreeWalk(repository)) {
				treeWalk.addTree(tree);
				treeWalk.setRecursive(true);
				if(treeFilter != null) {
					treeWalk.setFilter(treeFilter);
				}
				while (treeWalk.next()) {
					GitBlobInfo fileInfo = new GitBlobInfo();
					fileInfo.setPath(treeWalk.getPathString());
					fileInfo.setName(treeWalk.getNameString());
					fileInfo.setFolder(treeWalk.isSubtree());
					
					// 文件内容
					ObjectId blobObjectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(blobObjectId);
					fileInfo.setContent(new String(loader.getBytes()));

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
