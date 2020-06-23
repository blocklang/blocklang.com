package com.blocklang.marketplace.runner.action;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.action.AbstractActionTest;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.data.RepoConfigJson;

/**
 * 
 * @author Zhengwei Jin
 *
 */
public class GetRepoConfigActionTest extends AbstractActionTest {

	private GetRepoConfigAction action;
	
	@BeforeEach
	public void setup() throws IOException {
		action = new GetRepoConfigAction(context);
	}
	
	@Test
	public void run_widget_ide_repo_not_found_blocklang_json() throws IOException {
		initGitRepo();
		assertThat(action.run()).isFalse();
	}

	@Test
	public void run_widget_ide_repo_blocklang_json_invalid() throws IOException {
		initGitRepo();
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		// 其中缺少必填的 api 属性
		var jsonContent = StreamUtils.copyToString(this.getClass().getResourceAsStream("widget_ide_repo_config_invalid.json"), Charset.defaultCharset());
		Files.writeString(sourceDirectory.resolve("blocklang.json"), jsonContent);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		assertThat(action.run()).isFalse();
	}
	
	@Test
	public void run_repo_and_category_was_changed() throws IOException {
		initGitRepo();
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		var jsonContent = StreamUtils.copyToString(this.getClass().getResourceAsStream("widget_ide_repo_config.json"), Charset.defaultCharset());
		Files.writeString(sourceDirectory.resolve(MarketplaceStore.BLOCKLANG_JSON), jsonContent);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		var config = JsonUtil.fromJsonObject(jsonContent, RepoConfigJson.class);
		config.setRepo("PROD");
		config.setCategory("WebAPI");
		Files.writeString(sourceDirectory.resolve(MarketplaceStore.BLOCKLANG_JSON), JsonUtil.stringify(config));
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "second commit");
		
		assertThat(action.run()).isFalse();
	}
	
	@Test
	public void run_repo_and_category_was_allowed() throws IOException {
		initGitRepo();
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		InputStream in = getClass().getResourceAsStream("widget_ide_repo_config.json");
		var jsonContent = StreamUtils.copyToString(in, Charset.defaultCharset());
		var config = JsonUtil.fromJsonObject(jsonContent, RepoConfigJson.class);
		config.setRepo("INVALID-REPO");
		config.setCategory("INVALID_CATEGORY");
		Files.writeString(sourceDirectory.resolve(MarketplaceStore.BLOCKLANG_JSON), JsonUtil.stringify(config));
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "second commit");
		
		assertThat(action.run()).isFalse();
	}
	
	@Test
	public void run_if_repo_is_service_then_category_only_be_api() throws IOException {
		initGitRepo();
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		var jsonContent = StreamUtils.copyToString(this.getClass().getResourceAsStream("widget_ide_repo_config.json"), Charset.defaultCharset());
		var config = JsonUtil.fromJsonObject(jsonContent, RepoConfigJson.class);
		config.setRepo("Service");
		config.setCategory("Not-API");
		Files.writeString(sourceDirectory.resolve(MarketplaceStore.BLOCKLANG_JSON), JsonUtil.stringify(config));
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "second commit");
		
		assertThat(action.run()).isFalse();
	}

	@Test
	public void run_widget_ide_repo_success() throws IOException {
		initGitRepo();
		
		// 往仓库中 commit 一个 blocklang.json 文件
		Path sourceDirectory = store.getRepoSourceDirectory();
		var jsonContent = StreamUtils.copyToString(this.getClass().getResourceAsStream("widget_ide_repo_config.json"), Charset.defaultCharset());
		Files.writeString(sourceDirectory.resolve(MarketplaceStore.BLOCKLANG_JSON), jsonContent);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		assertThat(action.run()).isTrue();
		RepoConfigJson repoConfig = (RepoConfigJson) action.getOutput(GetRepoConfigAction.OUTPUT_REPO_CONFIG);
		assertThat(repoConfig.getName()).isEqualTo("ide-widgets");
		
		assertThat(action.getOutput("not-exist-key")).isNull();
	}

	private void initGitRepo() throws IOException {
		// 初始化一个 git 仓库
		Path sourceDirectory = store.getRepoSourceDirectory();
		Files.createDirectories(sourceDirectory);
		GitUtils.init(sourceDirectory, "user", "user@email.com");
	}
	
}
