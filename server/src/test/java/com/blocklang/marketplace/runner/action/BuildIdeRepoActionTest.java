package com.blocklang.marketplace.runner.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.action.AbstractActionTest;
import com.blocklang.core.runner.common.CliCommand;
import com.blocklang.core.runner.common.ExecutionContext;

/**
 * 构建所有 tag 和 master 分支
 * 
 * @author Zhengwei Jin
 *
 */
public class BuildIdeRepoActionTest extends AbstractActionTest{

	@DisplayName("如果没有指定 tag 或分支，则不 build，但仍返回 true")
	@Test
	public void run_tags_and_branches_is_empty() throws IOException {
		initIdeRepo();
		
		context.putValue("buildMaster", false);
		var action = new BuildIdeRepoAction(context);
		
		assertThat(action.run()).isTrue();
	}
	
	// store 的目录结构
	//
	// marketplace/
	//     github.com/
	//         you/
	//             your-repo/
	//                 source/
	//                     package.json
	//                     tsconfig.json
	//                     src/
	//                         main.ts
	//                  build/
	//                      package.json
	//                      tsconfig.json
	//                      src/
	//                          main.ts
	//                      output/
	//                          dist/
	//                              main.bundle.js
	//                   package
	//                       0.1.0
	//                           main.bundle.js
	//                       0.1.1
	//                           main.bundle.js
	@Test
	public void run_build_two_tags_success() throws IOException {
		Path sourceDirectory = initIdeRepo();
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		Files.writeString(sourceDirectory.resolve("src").resolve("index.html"), "<html></html>");
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "second commit");
		GitUtils.tag(sourceDirectory, "v0.1.1", "tag v0.1.1");
		
		context.putValue("buildMaster", false);
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		
		var actionSpy = spy(new BuildIdeRepoAction(context));
		var cliCommand = mock(CliCommand.class);
		Path buildDirectory = store.getRepoBuildDirectory();
		
		when(cliCommand.run(eq(buildDirectory), eq("yarn"))).thenReturn(true);
		
		when(cliCommand.run(eq(buildDirectory), eq("npm"), eq("run"), eq("build"))).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				// 模拟 dojo build app 命令，在 build/output/dist/ 目录下创建 main.bundle.js
				Path distDirectory = store.getRepoBuildDirectory().resolve("output").resolve("dist");
				Files.createDirectories(distDirectory);
				Files.writeString(distDirectory.resolve("main.bundle.js"), "console.log('Hello main.bundle.js')");
				return true;
			}
		});

		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		assertThat(actionSpy.run()).isTrue();
		// 断言 package/0.1.0 和 package/0.1.1 两个目录下有 main.bundle.js
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isTrue();
		assertThat(store.getPackageVersionDirectory("0.1.1").resolve("main.bundle.js").toFile().exists()).isTrue();
	
		// 因为构建了两个分支，所以执行次数为 2 x 2
		verify(cliCommand, times(4)).run(any(), any());
	}
	
	// 测试此用例，不需要创建 app
	@Test
	public void run_build_tags_ignore_build_if_tag_has_built() throws IOException {
		// v0.1.0 已构建过
		Path packageVersionDirectory = store.getPackageVersionDirectory("0.1.0");
		Files.createDirectories(packageVersionDirectory);
		Files.writeString(packageVersionDirectory.resolve("main.bundle.js"), "console.log('Hello main.bundle.js')");
		
		context.putValue("buildMaster", false);
		var actionSpy = spy(new BuildIdeRepoAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		// 都已构建时，则表明构建成功，而不是构建过程中出错。
		assertThat(actionSpy.run()).isTrue(); 
		// 断言 package/0.1.0 目录下有 main.bundle.js
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isTrue();
	
		// 因为已构建过，所以不会执行 build
		verify(cliCommand, never()).run(any(), any());
	}
	
	// 如果构建 tag 失败，则停止本 tag 的构建，但是要接着构建下一个 tag，
	// 这样能够一次发现所有 tag 和 master 分支下的错误
	// 但是出错的 tag 不能在文件系统中存储（存储就表示构建成功）
	@Test
	public void run_build_tags_a_tag_build_failed_then_build_next_tag() throws IOException {
		Path sourceDirectory = initIdeRepo();
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		// 注意，如果测试用例需要在不同 tag 之间 checkout 时，则不能将两个 tag 打在同一个 commitId 上，
		// 否则 checkout 无效
		Files.writeString(sourceDirectory.resolve("src").resolve("index.html"), "<html></html>");
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "second commit");
		GitUtils.tag(sourceDirectory, "v0.2.0", "tag v0.2.0");
		
		context.putValue("buildMaster", false);

		var actionSpy = spy(new BuildIdeRepoAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		Path buildDirectory = store.getRepoBuildDirectory();
		when(cliCommand.run(eq(buildDirectory), eq("yarn"))).thenReturn(true);
		
		when(cliCommand.run(eq(buildDirectory), eq("npm"), eq("run"), eq("build"))).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				// 在此处模拟 v0.1.0 构建失败，但 v0.2.0 构建失败
				// 如果当前 tag 是 v0.1.0 则返回 false，否则返回 true
				String tag = GitUtils.getCurrentTag(sourceDirectory);
				if("v0.1.0".equals(tag)) {
					return false;
				}
				
				// 模拟 dojo build app 命令，在 build/output/dist/ 目录下创建 main.bundle.js
				Path distDirectory = store.getRepoBuildDirectory().resolve("output").resolve("dist");
				Files.createDirectories(distDirectory);
				Files.writeString(distDirectory.resolve("main.bundle.js"), "console.log('Hello main.bundle.js')");
				return true;
			}
		});
		
		// 因为 build 第一个 tag 出错了，所以应该返回 false
		assertThat(actionSpy.run()).isFalse();
		// 0.1.0 build 失败
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isFalse();
		// 0.2.0 build 成功
		assertThat(store.getPackageVersionDirectory("0.2.0").resolve("main.bundle.js").toFile().exists()).isTrue();
		verify(cliCommand, times(4)).run(any(), any());
	}
	
	// 模拟将 build 文件夹中的文件复制到 package/{version} 文件夹
	// 这里通过删除 package.json，来让复制操作出错
	@Test
	public void run_build_tags_copy_from_build_to_package_failed() throws IOException {
		Path sourceDirectory = initIdeRepo();
		// 删除 package.json 文件，这样在移动时就会报错
		Files.deleteIfExists(sourceDirectory.resolve("package.json"));
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		context.putValue("buildMaster", false);
		var actionSpy = spy(new BuildIdeRepoAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		assertThat(actionSpy.run()).isFalse();
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isFalse();
		verify(cliCommand, never()).run(any(), any());
	}
	
	@Test
	public void run_build_tags_run_yarn_command_failed() throws IOException {
		Path sourceDirectory = initIdeRepo();
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		context.putValue("buildMaster", false);
		
		var actionSpy = spy(new BuildIdeRepoAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		Path buildDirectory = store.getRepoBuildDirectory();
		when(cliCommand.run(eq(buildDirectory), eq("yarn"))).thenReturn(false);
		
		assertThat(actionSpy.run()).isFalse();
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isFalse();
		verify(cliCommand).run(any(), any());
	}
	
	@Test
	public void run_build_tags_run_npm_run_build_failed() throws IOException {
		Path sourceDirectory = initIdeRepo();
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		context.putValue("buildMaster", false);
		
		var actionSpy = spy(new BuildIdeRepoAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		Path buildDirectory = store.getRepoBuildDirectory();
		when(cliCommand.run(eq(buildDirectory), eq("yarn"))).thenReturn(true);
		
		when(cliCommand.run(eq(buildDirectory), eq("npm"), eq("run"), eq("build"))).thenReturn(false);
		
		assertThat(actionSpy.run()).isFalse();
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isFalse();
		verify(cliCommand, times(2)).run(any(), any());
	}

	// 为了模拟错误，将 build/output/dist 文件夹删掉
	// 也就是在 spy 的 CliCommand.run 中不创建 dist 文件夹
	@Test
	public void run_build_tags_copy_from_build_output_dist_to_package_version_failed() throws IOException {
		Path sourceDirectory = initIdeRepo();
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		context.putValue("buildMaster", false);
		
		var actionSpy = spy(new BuildIdeRepoAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		Path buildDirectory = store.getRepoBuildDirectory();
		when(cliCommand.run(eq(buildDirectory), eq("yarn"))).thenReturn(true);
		
		// 注意，此处没有模拟创建 build/output/dist/main.bundle.js 文件
		when(cliCommand.run(eq(buildDirectory), eq("npm"), eq("run"), eq("build"))).thenReturn(true);
		
		assertThat(actionSpy.run()).isFalse();
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isFalse();
		verify(cliCommand, times(2)).run(any(), any());
	}
	
	@Test
	public void run_build_master_no_tags_success() throws IOException {
		Path sourceDirectory = initIdeRepo();
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");

		var actionSpy = spy(new BuildIdeRepoAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		Path buildDirectory = store.getRepoBuildDirectory();
		when(cliCommand.run(eq(buildDirectory), eq("yarn"))).thenReturn(true);
		
		when(cliCommand.run(eq(buildDirectory), eq("npm"), eq("run"), eq("build"))).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				// 模拟 dojo build app 命令，在 build/output/dist/ 目录下创建 main.bundle.js
				Path distDirectory = store.getRepoBuildDirectory().resolve("output").resolve("dist");
				Files.createDirectories(distDirectory);
				Files.writeString(distDirectory.resolve("main.bundle.js"), "console.log('Hello main.bundle.js')");
				return true;
			}
		});
		
		assertThat(actionSpy.run()).isTrue();
		assertThat(store.getPackageVersionDirectory("master").resolve("main.bundle.js").toFile().exists()).isTrue();
		verify(cliCommand, times(2)).run(any(), any());
	}
	
	// 如果没有构建 master，则也要确保切换到 master 分支
	@Test
	public void run_build_only_tags_ensure_chekout_to_master() throws IOException {
		Path sourceDirectory = initIdeRepo();
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		context.putValue("buildMaster", false);

		var actionSpy = spy(new BuildIdeRepoAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		Path buildDirectory = store.getRepoBuildDirectory();
		when(cliCommand.run(eq(buildDirectory), eq("yarn"))).thenReturn(true);
		
		when(cliCommand.run(eq(buildDirectory), eq("npm"), eq("run"), eq("build"))).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				// 模拟 dojo build app 命令，在 build/output/dist/ 目录下创建 main.bundle.js
				Path distDirectory = store.getRepoBuildDirectory().resolve("output").resolve("dist");
				Files.createDirectories(distDirectory);
				Files.writeString(distDirectory.resolve("main.bundle.js"), "console.log('Hello main.bundle.js')");
				return true;
			}
		});
		
		assertThat(actionSpy.run()).isTrue();
		assertThat(store.getPackageVersionDirectory("master").resolve("main.bundle.js").toFile().exists()).isFalse();
		verify(cliCommand, times(2)).run(any(), any());
		
		// 断言当前为 master 分支
		assertThat(GitUtils.getCurrentBranch(sourceDirectory)).isEqualTo("master");
	}
	
	private Path initIdeRepo() throws IOException {
		Path sourceDirectory = store.getRepoSourceDirectory();
		Files.createDirectories(sourceDirectory);
		GitUtils.init(sourceDirectory, "user", "user@email.com");
		Files.writeString(sourceDirectory.resolve("package.json"), "{}");
		Files.writeString(sourceDirectory.resolve("tsconfig.json"), "{}");
		
		Files.createDirectory(sourceDirectory.resolve("src"));
		Files.writeString(sourceDirectory.resolve("src").resolve("main.ts"), "console.log('Hello main.ts')");
		return sourceDirectory;
	}
	
}
