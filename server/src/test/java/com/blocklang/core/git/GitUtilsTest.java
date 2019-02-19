package com.blocklang.core.git;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.jgit.lib.Ref;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private String gitRepoDirectory = "gitRepo";
	private String gitUserName = "user";
	private String gitUserMail = "user@email.com";
	
	@Test
	public void is_git_repo_folder_not_exist() throws IOException {
		File folder = tempFolder.newFolder(gitRepoDirectory);
		assertThat(GitUtils.isGitRepo(folder.toPath().resolve("not-exist-folder"))).isFalse();
	}
	
	@Test
	public void is_git_repo_is_not_a_folder() throws IOException {
		File file = tempFolder.newFile(gitRepoDirectory);
		assertThat(GitUtils.isGitRepo(file.toPath())).isFalse();
	}
	
	@Test
	public void git_init_success() throws IOException {
		File folder = tempFolder.newFolder(gitRepoDirectory);
		
		assertThat(GitUtils.isGitRepo(folder.toPath())).isFalse();
		
		GitUtils.init(folder.toPath(), gitUserName, gitUserMail);
		
		assertThat(GitUtils.isGitRepo(folder.toPath())).isTrue();
	}
	
	@Test
	public void git_commit_success() throws IOException {
		File folder = tempFolder.newFolder(gitRepoDirectory);
		GitUtils.init(folder.toPath(), gitUserName, gitUserMail);
		
		// 在初始化时有一次 commit
		assertThat(GitUtils.getLogCount(folder.toPath())).isEqualTo(1);
		
		GitUtils.commit(folder.toPath(), "/a/b", "c.txt", "hello", "usera", "usera@email.com", "firstCommit");
		assertThat(GitUtils.getLogCount(folder.toPath())).isEqualTo(2);
		assertContentEquals(folder.toPath().resolve("a").resolve("b").resolve("c.txt"), "hello");
		
		GitUtils.commit(folder.toPath(), "/a/b", "c.txt", "hello world", "usera", "usera@email.com", "secondCommit");
		assertThat(GitUtils.getLogCount(folder.toPath())).isEqualTo(3);
		assertContentEquals(folder.toPath().resolve("a").resolve("b").resolve("c.txt"), "hello world");
	}
	
	@Test
	public void git_tag_success() throws IOException {
		// 新建一个 git 仓库
		File folder = tempFolder.newFolder(gitRepoDirectory);
		GitUtils.init(folder.toPath(), gitUserName, gitUserMail);
		
		// 断言仓库的标签数
		assertThat(GitUtils.getTagCount(folder.toPath())).isEqualTo(0);
		// 为 git 仓库打标签
		GitUtils.tag(folder.toPath(), "v0.1.0", "message");
		// 断言仓库的标签数
		assertThat(GitUtils.getTagCount(folder.toPath())).isEqualTo(1);
	}
	
	@Test
	public void get_tag_success() throws IOException {
		// 新建一个 git 仓库
		File folder = tempFolder.newFolder(gitRepoDirectory);
		GitUtils.init(folder.toPath(), gitUserName, gitUserMail);
		
		// 为 git 仓库打标签
		Ref existTag = GitUtils.tag(folder.toPath(), "v0.1.0", "message");
		// 获取仓库标签
		// 刚才添加的标签
		Optional<Ref> getedTag = GitUtils.getTag(folder.toPath(), "v0.1.0");
		assertThat(existTag.getObjectId().getName()).isEqualTo(getedTag.get().getObjectId().getName());
		
		// 不存在的标签
		getedTag = GitUtils.getTag(folder.toPath(), "v0.1.0-1");
		assertThat(getedTag.isEmpty()).isTrue();
	}

	private void assertContentEquals(Path filePath, String content) throws IOException{
		assertThat(Files.readString(filePath)).isEqualTo(content);
	}

}
