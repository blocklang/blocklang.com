package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class MarketplacePublishContextTest {

	private MarketplacePublishContext context;
	
	@Before
	public void setUp() {
		context = new MarketplacePublishContext(
				"c:/blocklang",
				"https://github.com/jack/app.git");
	}
	
	@Test
	public void get_repo_source_directory() {
		assertThat(context.getRepoSourceDirectory().compareTo(Paths.get("c:/blocklang/marketplace/github.com/jack/app/source"))).isEqualTo(0);
	}
	
	@Test
	public void get_repo_publish_log_file() {
		String version = "0.1.0";
		Path logFile = context.getRepoPublishLogFile(version);
		assertThat(logFile.getParent().compareTo(Paths.get("c:/blocklang/marketplace/github.com/jack/app/publishLogs"))).isEqualTo(0);
		
		String fileName = logFile.getFileName().toString();
		assertThat(fileName).startsWith("0.1.0-").endsWith(".log");
	}
	
	// 在同一个上下文中，调用两次获取日志文件的方式时，返回的日志文件应该是同一个
	@Test
	public void get_repo_publish_log_file_call_twice() throws InterruptedException {
		String version = "0.1.0";
		String fileName1 = context.getRepoPublishLogFile(version).getFileName().toString();
		TimeUnit.SECONDS.sleep(1);
		String fileName2 = context.getRepoPublishLogFile(version).getFileName().toString();

		assertThat(fileName1).isEqualTo(fileName2);
	}
}
