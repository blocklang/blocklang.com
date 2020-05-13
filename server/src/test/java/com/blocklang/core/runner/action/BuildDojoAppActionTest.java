package com.blocklang.core.runner.action;

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
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliCommand;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.DefaultExecutionContext;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.test.TestHelper;
import com.blocklang.marketplace.data.MarketplaceStore;

/**
 * 构建所有 tag 和 master 分支
 * 
 * @author Zhengwei Jin
 *
 */
public class BuildDojoAppActionTest {

	private ExecutionContext context;
	
	@BeforeEach
	public void setup() {
		context = new DefaultExecutionContext();
		var logger = mock(CliLogger.class);
		context.setLogger(logger);
	}
	
	@DisplayName("如果没有指定 tag 或分支，则不 build，但仍返回 true")
	@Test
	public void run_tags_and_branches_is_empty() {
		var action = new BuildDojoAppAction(context);
		assertThat(action.run()).isPresent();
	}
	
	// store 的目录结构
	//
	// root/
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
	public void run_build_tags_success(@TempDir Path tempDir) throws IOException {
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");
		
		Path sourceDirectory = createDojoApp(store);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		Files.writeString(sourceDirectory.resolve("src").resolve("index.html"), "<html></html>");
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "second commit");
		GitUtils.tag(sourceDirectory, "v0.1.1", "tag v0.1.1");
		
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		context.putValue("tags", Arrays.asList("refs/tags/v0.1.0", "refs/tags/v0.1.1"));
		
		var actionSpy = spy(new BuildDojoAppAction(context));
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
		
		assertThat(actionSpy.run()).isPresent();
		// 断言 package/0.1.0 和 package/0.1.1 两个目录下有 main.bundle.js
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isTrue();
		assertThat(store.getPackageVersionDirectory("0.1.1").resolve("main.bundle.js").toFile().exists()).isTrue();
	
		// 因为构建了两个分支，所以执行次数为 2 x 2
		verify(cliCommand, times(4)).run(any(), any());
		
		TestHelper.clearDir(tempDir);
	}
	
	// 测试此用例，不需要创建 dojo app
	@Test
	public void run_build_tags_ignore_build_if_has_built(@TempDir Path tempDir) throws IOException {
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");

		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		context.putValue("tags", Arrays.asList("refs/tags/v0.1.0", "refs/tags/v0.1.0"));
		
		// v0.1.0 已构建过
		Path packageVersionDirectory = store.getPackageVersionDirectory("0.1.0");
		Files.createDirectories(packageVersionDirectory);
		Files.writeString(packageVersionDirectory.resolve("main.bundle.js"), "console.log('Hello main.bundle.js')");
		
		var actionSpy = spy(new BuildDojoAppAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		assertThat(actionSpy.run()).isPresent(); // 都已构建时，则表明构建成功，而不是构建过程中出错。
		// 断言 package/0.1.0 和 package/0.1.1 两个目录下有 main.bundle.js
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isTrue();
	
		verify(cliCommand, never()).run(any(), any());
		
		TestHelper.clearDir(tempDir);
	}
	
	// 如果切换 tag 失败，则停止构建本 tag，但要接着构建下一个 tag
	@Test
	public void run_build_tags_checkout_to_tag_failed(@TempDir Path tempDir) throws IOException {
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");
		
		Path sourceDirectory = createDojoApp(store);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		// 只有 v0.1.0，却要构建 v0.1.1
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		context.putValue("tags", Collections.singletonList("refs/tags/v0.1.1"));
		
		var actionSpy = spy(new BuildDojoAppAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		assertThat(actionSpy.run()).isEmpty();
		// 断言 package/0.1.1 目录下没有 main.bundle.js
		assertThat(store.getPackageVersionDirectory("0.1.1").resolve("main.bundle.js").toFile().exists()).isFalse();
		verify(cliCommand, never()).run(any(), any());
		
		TestHelper.clearDir(tempDir);
	}
	
	// 模拟将 build 文件夹中的文件复制到 package/{version} 文件夹
	// 这里通过删除 package.json，来让复制操作出错
	@Test
	public void run_build_tags_copy_from_build_to_package_failed(@TempDir Path tempDir) throws IOException {
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");
		
		Path sourceDirectory = createDojoApp(store);
		// 删除 package.json 文件，这样在移动时就会报错
		Files.deleteIfExists(sourceDirectory.resolve("package.json"));
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		context.putValue("tags", Collections.singletonList("refs/tags/v0.1.0"));
		
		var actionSpy = spy(new BuildDojoAppAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		assertThat(actionSpy.run()).isEmpty();
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isFalse();
		verify(cliCommand, never()).run(any(), any());
		
		TestHelper.clearDir(tempDir);
	}
	
	@Test
	public void run_build_tags_run_yarn_command_failed(@TempDir Path tempDir) throws IOException {
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");
		
		Path sourceDirectory = createDojoApp(store);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		context.putValue("tags", Collections.singletonList("refs/tags/v0.1.0"));
		
		var actionSpy = spy(new BuildDojoAppAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		Path buildDirectory = store.getRepoBuildDirectory();
		when(cliCommand.run(eq(buildDirectory), eq("yarn"))).thenReturn(false);
		
		assertThat(actionSpy.run()).isEmpty();
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isFalse();
		verify(cliCommand).run(any(), any());
		
		TestHelper.clearDir(tempDir);
	}
	
	@Test
	public void run_build_tags_run_npm_run_build_failed(@TempDir Path tempDir) throws IOException {
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");
	
		Path sourceDirectory = createDojoApp(store);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		context.putValue("tags", Collections.singletonList("refs/tags/v0.1.0"));
		
		var actionSpy = spy(new BuildDojoAppAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		Path buildDirectory = store.getRepoBuildDirectory();
		when(cliCommand.run(eq(buildDirectory), eq("yarn"))).thenReturn(true);
		
		when(cliCommand.run(eq(buildDirectory), eq("npm"), eq("run"), eq("build"))).thenReturn(false);
		
		assertThat(actionSpy.run()).isEmpty();
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isFalse();
		verify(cliCommand, times(2)).run(any(), any());
		
		TestHelper.clearDir(tempDir);
	}

	// 为了模拟错误，将 build/output/dist 文件夹删掉
	// 也就是在 spy 的 CliCommand.run 中不创建 dist 文件夹
	@Test
	public void run_build_tags_copy_from_build_output_dist_to_package_version_failed(@TempDir Path tempDir) throws IOException {
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");
		
		Path sourceDirectory = createDojoApp(store);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "tag v0.1.0");
		
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		context.putValue("tags", Collections.singletonList("refs/tags/v0.1.0"));
		
		var actionSpy = spy(new BuildDojoAppAction(context));
		var cliCommand = mock(CliCommand.class);
		doReturn(cliCommand).when(actionSpy).getCliCommand();
		
		Path buildDirectory = store.getRepoBuildDirectory();
		when(cliCommand.run(eq(buildDirectory), eq("yarn"))).thenReturn(true);
		
		// 注意，此处没有模拟创建 build/output/dist/main.bundle.js 文件
		when(cliCommand.run(eq(buildDirectory), eq("npm"), eq("run"), eq("build"))).thenReturn(true);
		
		assertThat(actionSpy.run()).isEmpty();
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve("main.bundle.js").toFile().exists()).isFalse();
		verify(cliCommand, times(2)).run(any(), any());
		
		TestHelper.clearDir(tempDir);
	}
	
	private Path createDojoApp(MarketplaceStore store) throws IOException {
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
