package com.blocklang.core.git;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.blocklang.core.constant.GitFileStatus;

/**
 * git 测试用例
 * 
 * 实现和测试用例，参考 
 * 
 * https://dev.tencent.com/u/jinzw/p/doufuding/git/tree/master/src/main/java/com/doufuding/math/git
 * 
 * https://dev.tencent.com/u/jinzw/p/doufuding/git/blob/master/src/test/java/com/doufuding/math/git/GitUtilsTest.java
 * 
 * @author Zhengwei Jin
 */
public class GitUtilsTest {

	private String gitRepoDirectory = "gitRepo";
	private String gitUserName = "user";
	private String gitUserMail = "user@email.com";
	
	@Test
	public void is_git_repo_folder_not_exist(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		assertThat(GitUtils.isGitRepo(folder.resolve("not-exist-folder"))).isFalse();
	}
	
	@Test
	public void is_git_repo_is_not_a_folder(@TempDir Path tempDir) throws IOException {
		Path file = tempDir.resolve(gitRepoDirectory);
		Files.createFile(file);
		
		assertThat(GitUtils.isGitRepo(file)).isFalse();
	}
	
	@Test
	public void git_init_success(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		assertThat(GitUtils.isGitRepo(folder)).isFalse();
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		assertThat(GitUtils.isGitRepo(folder)).isTrue();
		
		
	}
	
	@Test
	public void git_init_with_files_success(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.beginInit(folder, gitUserName, gitUserMail).addFile("a.txt", "Hello").commit("first commit");
		assertContentEquals(folder.resolve("a.txt"), "Hello");
		
		
	}
	
	@Test
	public void git_commit_success(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		// 在初始化时有一次 commit
		assertThat(GitUtils.getLogCount(folder)).isEqualTo(1);
		
		GitUtils.commit(folder, "/a/b", "c.txt", "hello", "usera", "usera@email.com", "firstCommit");
		assertThat(GitUtils.getLogCount(folder)).isEqualTo(2);
		assertContentEquals(folder.resolve("a").resolve("b").resolve("c.txt"), "hello");
		
		GitUtils.commit(folder, "/a/b", "c.txt", "hello world", "usera", "usera@email.com", "secondCommit");
		assertThat(GitUtils.getLogCount(folder)).isEqualTo(3);
		assertContentEquals(folder.resolve("a").resolve("b").resolve("c.txt"), "hello world");
		
		
	}
	
	@Test
	public void git_tag_success(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		// 断言仓库的标签数
		assertThat(GitUtils.getTagCount(folder)).isEqualTo(0);
		// 为 git 仓库打标签
		GitUtils.tag(folder, "v0.1.0", "message");
		// 断言仓库的标签数
		assertThat(GitUtils.getTagCount(folder)).isEqualTo(1);
		
		
	}
	
	@Test
	public void get_tag_success(@TempDir Path tempDir) throws IOException {
		// 新建一个 git 仓库
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		// 为 git 仓库打标签
		Ref existTag = GitUtils.tag(folder, "v0.1.0", "message");
		// 获取仓库标签
		// 刚才添加的标签
		Optional<Ref> getedTag = GitUtils.getTag(folder, "v0.1.0");
		assertThat(existTag.getObjectId().getName()).isEqualTo(getedTag.get().getObjectId().getName());
		
		// 不存在的标签
		getedTag = GitUtils.getTag(folder, "v0.1.0-1");
		assertThat(getedTag.isEmpty()).isTrue();
		
		
	}
	
	@Test
	public void get_latest_commit_at_root(@TempDir Path tempDir) throws IOException {
		// 新建一个 git 仓库
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		GitUtils.commit(folder, null, "a.txt", "hello", gitUserName, gitUserMail, "commit 1");
		RevCommit latestCommit = GitUtils.getLatestCommit(folder);
		assertThat(latestCommit.getFullMessage()).isEqualTo("commit 1");
		
		GitUtils.commit(folder, null, "a.txt", "hello world", gitUserName, gitUserMail, "commit 2");
		latestCommit = GitUtils.getLatestCommit(folder);
		assertThat(latestCommit.getFullMessage()).isEqualTo("commit 2");
		
		
	}
	
	@Test
	public void get_latest_commit_at_sub_folder(@TempDir Path tempDir) throws IOException {
		// 新建一个 git 仓库
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		GitUtils.commit(folder, "a", "a.txt", "hello", gitUserName, gitUserMail, "commit 1");
		RevCommit latestCommit = GitUtils.getLatestCommit(folder, "a");
		assertThat(latestCommit.getFullMessage()).isEqualTo("commit 1");
		
		GitUtils.commit(folder, "a", "a.txt", "hello world", gitUserName, gitUserMail, "commit 2");
		latestCommit = GitUtils.getLatestCommit(folder, "a");
		assertThat(latestCommit.getFullMessage()).isEqualTo("commit 2");
		
		
	}
	
	@Test
	public void get_files_in_invalid_repo(@TempDir Path tempDir) throws IOException {
		// 新建一个 git 仓库
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);

		List<GitFileInfo> gitFiles = GitUtils.getFiles(folder, null);
		
		assertThat(gitFiles).isEmpty();
		
		
	}
	
	// 只适用于 master 分支
	@Test
	public void get_files_at_root(@TempDir Path tempDir) throws IOException {
		// 新建一个 git 仓库
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		String commitId1 = GitUtils.commit(folder, null, "1.txt", "hello", gitUserName, gitUserMail, "commit 1");
		String commitId2 = GitUtils.commit(folder, null, "2.txt", "world", gitUserName, gitUserMail, "commit 2");
		
		List<GitFileInfo> gitFiles = GitUtils.getFiles(folder, null);

		assertThat(gitFiles).hasSize(2).anyMatch(gitFile -> {
			return gitFile.isFolder() == false &&
					gitFile.getCommitId().equals(commitId1) &&
					gitFile.getLatestShortMessage().equals("commit 1") &&
					gitFile.getLatestFullMessage().equals("commit 1") &&
					gitFile.getName().equals("1.txt") &&
					gitFile.getPath().equals("1.txt") &&
					gitFile.getLatestCommitTime() != null;
		}).anyMatch(gitFile -> {
			return gitFile.isFolder() == false &&
					gitFile.getCommitId().equals(commitId2) &&
					gitFile.getLatestShortMessage().equals("commit 2") &&
					gitFile.getLatestFullMessage().equals("commit 2") &&
					gitFile.getName().equals("2.txt") &&
					gitFile.getPath().equals("2.txt") &&
					gitFile.getLatestCommitTime() != null;
		});
		
		
	}
	
	// 只适用于 master 分支
	@Test
	public void get_files_folder_not_commit(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		
		Files.createDirectory(folder);
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		List<GitFileInfo> gitFiles = GitUtils.getFiles(folder, "a-folder-not-commit");
		assertThat(gitFiles).isEmpty();
		
		
	}
	
	// 只适用于 master 分支
	@Test
	public void get_files_at_sub_folder(@TempDir Path tempDir) throws IOException {
		// 新建一个 git 仓库
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		String commitId1 = GitUtils.commit(folder, "a", "1.txt", "hello", gitUserName, gitUserMail, "commit 1");
		String commitId2 = GitUtils.commit(folder, "a", "2.txt", "world", gitUserName, gitUserMail, "commit 2");
		
		List<GitFileInfo> gitFiles = GitUtils.getFiles(folder, "a");

		assertThat(gitFiles).hasSize(2).anyMatch(gitFile -> {
			return gitFile.isFolder() == false &&
					gitFile.getCommitId().equals(commitId1) &&
					gitFile.getLatestShortMessage().equals("commit 1") &&
					gitFile.getLatestFullMessage().equals("commit 1") &&
					gitFile.getName().equals("1.txt") &&
					gitFile.getPath().equals("a/1.txt") &&
					gitFile.getLatestCommitTime() != null;
		}).anyMatch(gitFile -> {
			return gitFile.isFolder() == false &&
					gitFile.getCommitId().equals(commitId2) &&
					gitFile.getLatestShortMessage().equals("commit 2") &&
					gitFile.getLatestFullMessage().equals("commit 2") &&
					gitFile.getName().equals("2.txt") &&
					gitFile.getPath().equals("a/2.txt") &&
					gitFile.getLatestCommitTime() != null;
		});
		
		
	}
	
	@Test
	public void get_all_files_from_tag_ref_name_can_not_blank(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> GitUtils.readAllFileInfo(folder, null, null));
		assertThat(exception.getMessage()).isEqualTo("tag 的值不能为空");
		
		exception = Assertions.assertThrows(IllegalArgumentException.class, () -> GitUtils.readAllFileInfo(folder, " ", null));
		assertThat(exception.getMessage()).isEqualTo("tag 的值不能为空");
		
		
	}
	
	// 获取一个仓库中所有文件，不包括文件夹
	@Test
	public void get_all_files_from_tag_when_tag_not_exist(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		List<GitFileInfo> gitFiles = GitUtils.readAllFileInfo(folder, "refs/tags/v0.1.0", null);
		assertThat(gitFiles).isEmpty();
		
		
	}
	
	@Test
	public void get_all_files_from_tag_at_root(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		GitUtils.commit(folder, null, "1.txt", "hello", gitUserName, gitUserMail, "commit 1");
		GitUtils.tag(folder, "v0.1.0", "message");
		
		List<GitFileInfo> gitFiles = GitUtils.readAllFileInfo(folder, "refs/tags/v0.1.0", null);
		assertThat(gitFiles).hasSize(1);
		GitFileInfo file = gitFiles.get(0);
		assertThat(file.getPath()).isEqualTo("1.txt");
		assertThat(file.getName()).isEqualTo("1.txt");
		assertThat(file.getParentPath()).isEqualTo("");
		
		
	}
	
	@Test
	public void get_all_files_from_tag_at_sub_folder(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		GitUtils.commit(folder, "a", "1.txt", "hello", gitUserName, gitUserMail, "commit 1");
		GitUtils.tag(folder, "v0.1.0", "message");
		
		List<GitFileInfo> gitFiles = GitUtils.readAllFileInfo(folder, "refs/tags/v0.1.0", null);
		assertThat(gitFiles).hasSize(1);
		GitFileInfo file = gitFiles.get(0);
		assertThat(file.getPath()).isEqualTo("a/1.txt");
		assertThat(file.getName()).isEqualTo("1.txt");
		assertThat(file.getParentPath()).isEqualTo("a");
		
		
	}
	
	@Test
	public void get_all_files_from_tag_at_sub_folder_filter(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		GitUtils.commit(folder, "a", "1.txt", "hello", gitUserName, gitUserMail, "commit 1");
		GitUtils.tag(folder, "v0.1.0", "message");
		
		List<GitFileInfo> gitFiles = GitUtils.readAllFileInfo(folder, "refs/tags/v0.1.0", ".json");
		assertThat(gitFiles).isEmpty();
		
		gitFiles = GitUtils.readAllFileInfo(folder, "refs/tags/v0.1.0", ".txt");
		assertThat(gitFiles).hasSize(1);
		GitFileInfo file = gitFiles.get(0);
		assertThat(file.getPath()).isEqualTo("a/1.txt");
		assertThat(file.getName()).isEqualTo("1.txt");
		assertThat(file.getParentPath()).isEqualTo("a");
		
		
	}
	
	@Test
	public void get_all_files_from_tag_at_two_tags(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		GitUtils.commit(folder, null, "1.txt", "hello", gitUserName, gitUserMail, "commit 1");
		GitUtils.tag(folder, "v0.1.0", "message");
		
		GitUtils.commit(folder, null, "2.txt", "hello", gitUserName, gitUserMail, "commit 1");
		GitUtils.tag(folder, "v0.2.0", "message");
		
		List<GitFileInfo> gitFiles = GitUtils.readAllFileInfo(folder, "refs/tags/v0.1.0", null);
		assertThat(gitFiles).hasSize(1);
		GitFileInfo file = gitFiles.get(0);
		assertThat(file.getPath()).isEqualTo("1.txt");
		assertThat(file.getName()).isEqualTo("1.txt");
		assertThat(file.getParentPath()).isEqualTo("");
		
		gitFiles = GitUtils.readAllFileInfo(folder, "refs/tags/v0.2.0", null);
		assertThat(gitFiles).hasSize(2);
		
		
	}
	
	@Test
	public void status_untracked(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		Path file1 = folder.resolve("file1");
		Files.createFile(file1);
		
		Path subFolder = folder.resolve("a");
		Path file2 = subFolder.resolve("file2");
		Files.createDirectory(subFolder);
		Files.createFile(file2);
		
		Map<String, GitFileStatus> status = GitUtils.status(folder, null);
		assertThat(status).hasSize(3).containsKeys("a", "file1", "a/file2").containsValue(GitFileStatus.UNTRACKED);
		
		status = GitUtils.status(folder, "a");
		assertThat(status).hasSize(2).containsKeys("a", "a/file2").containsValue(GitFileStatus.UNTRACKED);
		
		
	}
	
	@Test
	public void status_added(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		Path file1 = folder.resolve("file1");
		Files.createFile(file1);
		GitUtils.add(folder, "file1");
		
		Path subFolder = folder.resolve("a");
		Path file2 = subFolder.resolve("file2");
		Files.createDirectory(subFolder);
		Files.createFile(file2);
		GitUtils.add(folder, "a/file2");
		
		Map<String, GitFileStatus> status = GitUtils.status(folder, null);
		assertThat(status).hasSize(2).containsKeys("file1", "a/file2").containsValue(GitFileStatus.ADDED).doesNotContainValue(GitFileStatus.UNTRACKED);
		
		status = GitUtils.status(folder, "a");
		assertThat(status).hasSize(1).containsKeys("a/file2").containsValue(GitFileStatus.ADDED);
		
		
	}
	
	@Test
	public void status_all_commited(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		GitUtils.commit(folder, null, "file1", "hello", gitUserName, gitUserMail, "commit 1");
		GitUtils.commit(folder, "a", "file2", "hello", gitUserName, gitUserMail, "commit 1");
		
		Map<String, GitFileStatus> status = GitUtils.status(folder, null);
		assertThat(status).isEmpty();
		
		status = GitUtils.status(folder, "a");
		assertThat(status).isEmpty();
		
		
	}
	
	@Test
	public void status_modified(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		GitUtils.commit(folder, null, "file1", "hello", gitUserName, gitUserMail, "commit 1");
		GitUtils.commit(folder, "a", "file2", "hello", gitUserName, gitUserMail, "commit 1");
		
		Path file1 = folder.resolve("file1");
		Files.writeString(file1, " world", StandardOpenOption.APPEND);
		
		Path file2 = folder.resolve("a").resolve("file2");
		Files.writeString(file2, " world", StandardOpenOption.APPEND);
		
		Map<String, GitFileStatus> status = GitUtils.status(folder, null);
		assertThat(status).hasSize(2).containsKeys("file1", "a/file2").containsValue(GitFileStatus.MODIFIED);
		
		status = GitUtils.status(folder, "a");
		assertThat(status).hasSize(1).containsKeys("a/file2").containsValue(GitFileStatus.MODIFIED);
		
		
	}
	
	@Test
	public void status_deleted(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		GitUtils.commit(folder, null, "file1", "hello", gitUserName, gitUserMail, "commit 1");
		GitUtils.commit(folder, "a", "file2", "hello", gitUserName, gitUserMail, "commit 1");
		
		Path file1 = folder.resolve("file1");
		Files.delete(file1);
		
		Path file2 = folder.resolve("a").resolve("file2");
		Files.delete(file2);
		
		Map<String, GitFileStatus> status = GitUtils.status(folder, null);
		assertThat(status).hasSize(2).containsKeys("file1", "a/file2").containsValue(GitFileStatus.DELETED);
		
		status = GitUtils.status(folder, "a");
		assertThat(status).hasSize(1).containsKeys("a/file2").containsValue(GitFileStatus.DELETED);
		
		
	}
	
	@Test
	public void remove_success(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		Path file1 = folder.resolve("file1");
		Files.createFile(file1);
		GitUtils.add(folder, "file1");
		
		
		Path subFolder = folder.resolve("a");
		Path file2 = subFolder.resolve("file2");
		Files.createDirectory(subFolder);
		Files.createFile(file2);
		GitUtils.add(folder, "a/file2");
		
		assertThat(file1).exists();
		assertThat(file2).exists();
		
		GitUtils.remove(folder, "file1");
		GitUtils.remove(folder, "a/file2");
		
		assertThat(file1).doesNotExist();
		assertThat(file2).doesNotExist();
		
		
	}
	
	@Test
	public void is_valid_remote_repository() {
		assertThat(GitUtils.isValidRemoteRepository(" ")).isFalse();
		assertThat(GitUtils.isValidRemoteRepository("x")).isFalse();
		assertThat(GitUtils.isValidRemoteRepository("https://github.com/blocklang/not-exist-repo.git")).isFalse();
		assertThat(GitUtils.isValidRemoteRepository("https://github.com/blocklang/blocklang.com.git")).isTrue();
	}
	
	@Test
	public void get_latest_tag_no_data(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		Optional<Ref> tagOption = GitUtils.getLatestTag(folder);
		assertThat(tagOption).isEmpty();
		
		
	}
	
	@Test
	public void get_latest_tag_only_one_tag(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		GitUtils.tag(folder, "v0.1.0", "message");
		
		Optional<Ref> tagOption = GitUtils.getLatestTag(folder);
		assertThat(tagOption).isPresent();
		assertThat(tagOption.get().getName()).isEqualTo("refs/tags/v0.1.0");
		
		
	}
	
	@Test
	public void get_latest_tag_two_tags(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		GitUtils.tag(folder, "v0.1.0", "message1");
		GitUtils.commit(folder, "", "c.txt", "hello", "usera", "usera@email.com", "firstCommit");
		GitUtils.tag(folder, "v0.1.1", "message2");
		
		Optional<Ref> tagOption = GitUtils.getLatestTag(folder);
		assertThat(tagOption).isPresent();
		assertThat(tagOption.get().getName()).isEqualTo("refs/tags/v0.1.1");
		
		
	}
	
	@Test
	public void get_tags(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		GitUtils.tag(folder, "v0.1.0", "message1");
		GitUtils.commit(folder, "", "c.txt", "hello", "usera", "usera@email.com", "firstCommit");
		GitUtils.tag(folder, "v0.1.1", "message2");
		
		List<Ref> tags = GitUtils.getTags(folder);
		assertThat(tags).hasSize(2);
		
		
	}
	
	@Test
	public void get_blob_from_branch_no_data(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		Optional<GitBlobInfo> blobOption = GitUtils.getBlob(folder, Constants.R_HEADS + Constants.MASTER, "a.txt");
		assertThat(blobOption).isEmpty();
		
		
	}
	
	@Test
	public void get_blob_from_branch_success(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		String commitId = GitUtils.commit(folder, "", "a.txt", "hello", "usera", "usera@email.com", "first commit");
		Optional<GitBlobInfo> blobOption = GitUtils.getBlob(folder, Constants.R_HEADS + Constants.MASTER, "a.txt");
		
		assertThat(blobOption).isPresent();
		
		GitBlobInfo blob = blobOption.get();
		assertThat(blob.getContent()).isEqualTo("hello");
		assertThat(blob.getPath()).isEqualTo("a.txt");
		assertThat(blob.getName()).isEqualTo("a.txt");
		assertThat(blob.getCommitId()).isEqualTo(commitId);
		assertThat(blob.getLatestShortMessage()).isEqualTo("first commit");
		assertThat(blob.getLatestFullMessage()).isEqualTo("first commit");
		assertThat(blob.getLatestCommitTime()).isNotNull();
		
		
	}
	
	@Test
	public void get_blob_from_tag_no_data(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		
		Optional<GitBlobInfo> blobOption = GitUtils.getBlob(folder, Constants.R_TAGS + "v0.1.0", "a.txt");
		assertThat(blobOption).isEmpty();
		
		
	}
	
	@Test
	public void get_blob_from_tag_success(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		String commitId = GitUtils.commit(folder, "", "a.txt", "hello", "usera", "usera@email.com", "first commit");
		GitUtils.tag(folder, "v0.1.0", "first tag");
		String commitId2 = GitUtils.commit(folder, "", "a.txt", "hello world", "usera", "usera@email.com", "second commit");
		GitUtils.tag(folder, "v0.1.1", "second tag");
		
		Optional<GitBlobInfo> blobOption = GitUtils.getBlob(folder, Constants.R_TAGS + "v0.1.0", "a.txt");
		assertThat(blobOption).isPresent();
		GitBlobInfo blob = blobOption.get();
		assertThat(blob.getContent()).isEqualTo("hello");
		assertThat(blob.getPath()).isEqualTo("a.txt");
		assertThat(blob.getName()).isEqualTo("a.txt");
		assertThat(blob.getCommitId()).isEqualTo(commitId);
		assertThat(blob.getLatestShortMessage()).isEqualTo("first commit");
		assertThat(blob.getLatestFullMessage()).isEqualTo("first commit");
		assertThat(blob.getLatestCommitTime()).isNotNull();
		
		blobOption = GitUtils.getBlob(folder, Constants.R_TAGS + "v0.1.1", "a.txt");
		assertThat(blobOption).isPresent();
		blob = blobOption.get();
		assertThat(blob.getContent()).isEqualTo("hello world");
		assertThat(blob.getPath()).isEqualTo("a.txt");
		assertThat(blob.getName()).isEqualTo("a.txt");
		assertThat(blob.getCommitId()).isEqualTo(commitId2);
		assertThat(blob.getLatestShortMessage()).isEqualTo("second commit");
		assertThat(blob.getLatestFullMessage()).isEqualTo("second commit");
		assertThat(blob.getLatestCommitTime()).isNotNull();
		
		
	}
	
	@Test
	public void load_data_param_is_null() throws IOException {
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> GitUtils.loadDataFromTag(null, Constants.R_TAGS + "v0.1.0", null));
		assertThat(exception.getMessage()).isEqualTo("传入的值不能为null");
	}
	
	@Test
	public void load_data_param_is_empty_list(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		assertThat(GitUtils.loadDataFromTag(folder, Constants.R_TAGS + "v0.1.0", Collections.emptyList())).isEmpty();
		
		
	}
	
	@Test
	public void load_data_success(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		GitUtils.commit(folder, null, "1.txt", "hello", gitUserName, gitUserMail, "commit 1");
		GitUtils.tag(folder, "v0.1.0", "message");
		
		List<GitFileInfo> gitFiles = GitUtils.readAllFileInfo(folder, "refs/tags/v0.1.0", null);
		
		List<GitBlobInfo> files = GitUtils.loadDataFromTag(folder, "refs/tags/v0.1.0", gitFiles);
		assertThat(files).hasSize(1);
		assertThat(files.get(0).getContent()).isEqualTo("hello");
	}
	
	@Test
	public void get_version_from_ref_name_when_tag_name_is_null() {
		assertThat(GitUtils.getVersionFromRefName(null)).isEmpty();
		assertThat(GitUtils.getVersionFromRefName(" ")).isEmpty();
	}
	
	@Test
	public void get_version_from_ref_name_success() {
		assertThat(GitUtils.getVersionFromRefName("0.1.0").get()).isEqualTo("0.1.0");
		assertThat(GitUtils.getVersionFromRefName("refs/tags/0.1.0").get()).isEqualTo("0.1.0");
		assertThat(GitUtils.getVersionFromRefName("refs/tags/v0.1.0").get()).isEqualTo("0.1.0");
	}
	
	@Test
	public void get_tag_name() {
		assertThat(GitUtils.getTagName(null)).isEmpty();
		assertThat(GitUtils.getTagName(" ")).isEmpty();
		assertThat(GitUtils.getTagName("refs/tags/v0.1.0").get()).isEqualTo("v0.1.0");
	}

	@Test
	public void checkout_tag_success(@TempDir Path tempDir) throws IOException {
		Path folder = tempDir.resolve(gitRepoDirectory);
		Files.createDirectory(folder);
		
		GitUtils.init(folder, gitUserName, gitUserMail);
		GitUtils.commit(folder, "", "a.txt", "hello", "usera", "usera@email.com", "firstCommit");
		GitUtils.tag(folder, "v0.1.0", "message1");
		GitUtils.commit(folder, "", "a.txt", "hello world", "usera", "usera@email.com", "firstCommit");
		
		// 直接从文件系统中获取
		// 先断言 a.txt 中的内容是 hello world
		assertContentEquals(folder.resolve("a.txt"), "hello world");
		// 然后切换到 v0.1.0 分支
		GitUtils.checkout(folder, "v0.1.0");
		// 再断言 a.txt 中的内容是 hello
		assertContentEquals(folder.resolve("a.txt"), "hello");
		
		// 最后再切换回 master 分支
		GitUtils.checkout(folder, "master");
		assertContentEquals(folder.resolve("a.txt"), "hello world");
	}
	
	private void assertContentEquals(Path filePath, String content) throws IOException{
		assertThat(Files.readString(filePath)).isEqualTo(content);
	}

}
