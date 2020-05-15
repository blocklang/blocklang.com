package com.blocklang.core.runner.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.StreamUtils;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.DefaultExecutionContext;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.test.TestHelper;
import com.blocklang.marketplace.data.MarketplaceStore;

/**
 * 
 * @author Zhengwei Jin
 *
 */
public class GetRepoConfigActionTest {

	private ExecutionContext context;
	private MarketplaceStore store;
	
	@BeforeEach
	public void setup() throws IOException {
		context = new DefaultExecutionContext();
		var logger = mock(CliLogger.class);
		context.setLogger(logger);
	}
	
	@Test
	public void new_should_set_inputs() {
		assertThrows(IllegalArgumentException.class, () -> new GetRepoConfigAction(context));
	}
	
	@Test
	public void run_widget_ide_repo_success(@TempDir Path tempDir) throws IOException {
		initGitRepo(tempDir);
		
		// 往仓库中 commit 一个 blocklang.json 文件
		Path sourceDirectory = store.getRepoSourceDirectory();
		var jsonContent = StreamUtils.copyToString(this.getClass().getResourceAsStream("widget_ide_repo_config.json"), Charset.defaultCharset());
		Files.writeString(sourceDirectory.resolve("blocklang.json"), jsonContent);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		Path repoConfigFile = store.getRepoConfigFile();
		context.putValue(GetRepoConfigAction.INPUT_CONFIG_FILE, repoConfigFile);
		var action = new GetRepoConfigAction(context);
		
		assertThat(action.run()).isPresent();
		
		TestHelper.clearDir(sourceDirectory);
	}
	
	@Test
	public void run_widget_ide_repo_not_found_blocklang_json(@TempDir Path tempDir) throws IOException {
		initGitRepo(tempDir);
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		
		Path repoConfigFile = store.getRepoConfigFile();
		context.putValue(GetRepoConfigAction.INPUT_CONFIG_FILE, repoConfigFile);
		var action = new GetRepoConfigAction(context);
		
		assertThat(action.run()).isEmpty();
		
		TestHelper.clearDir(sourceDirectory);
	}
	
	@Test
	public void run_widget_ide_repo_blocklang_json_invalid(@TempDir Path tempDir) throws IOException {
		initGitRepo(tempDir);
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		var jsonContent = StreamUtils.copyToString(this.getClass().getResourceAsStream("widget_ide_repo_config_invalid.json"), Charset.defaultCharset());
		Files.writeString(sourceDirectory.resolve("blocklang.json"), jsonContent);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		Path repoConfigFile = store.getRepoConfigFile();
		context.putValue(GetRepoConfigAction.INPUT_CONFIG_FILE, repoConfigFile);
		var action = new GetRepoConfigAction(context);
		
		assertThat(action.run()).isEmpty();
		
		TestHelper.clearDir(sourceDirectory);
	}

	private void initGitRepo(Path tempDir) throws IOException {
		store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		
		// 初始化一个 git 仓库
		Path sourceDirectory = store.getRepoSourceDirectory();
		Files.createDirectories(sourceDirectory);
		GitUtils.init(sourceDirectory, "user", "user@email.com");
	}
	
}
