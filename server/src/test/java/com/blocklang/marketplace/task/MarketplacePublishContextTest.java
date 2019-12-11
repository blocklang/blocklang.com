package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blocklang.marketplace.model.ComponentRepoPublishTask;

public class MarketplacePublishContextTest {

	private MarketplacePublishContext context;
	
	@BeforeEach
	public void setUp() {
		ComponentRepoPublishTask publishTask = new ComponentRepoPublishTask();
		publishTask.setGitUrl("https://github.com/jack/app.git");
		
		context = new MarketplacePublishContext(
				"c:/blocklang",
				publishTask);
	}
	
	@Test
	public void get_component_repo_publish_log_file() {
		Path logFile = context.getRepoPublishLogFile();
		assertThat(logFile.getParent().compareTo(Paths.get("c:/blocklang/marketplace/github.com/jack/app/publishLogs"))).isEqualTo(0);
		
		String fileName = logFile.getFileName().toString();
		assertThat(fileName).endsWith(".log");
	}
	
	// 在同一个上下文中，调用两次获取日志文件的方式时，返回的日志文件应该是同一个
	@Test
	public void get_component_repo_publish_log_file_call_twice() throws InterruptedException {
		String fileName1 = context.getRepoPublishLogFile().getFileName().toString();
		TimeUnit.SECONDS.sleep(1);
		String fileName2 = context.getRepoPublishLogFile().getFileName().toString();

		assertThat(fileName1).isEqualTo(fileName2);
	}
	
	@Test
	public void get_local_component_repo_info() {
		assertThat(context.getLocalComponentRepoPath()).isNotNull();
		assertThat(context.getLocalApiRepoPath()).isNull();
	}
	
	@Test
	public void get_local_api_repo_info() {
		context.parseApiGitUrl("https://github.com/jack/api.git");
		assertThat(context.getLocalApiRepoPath()).isNotNull();
	}

}
