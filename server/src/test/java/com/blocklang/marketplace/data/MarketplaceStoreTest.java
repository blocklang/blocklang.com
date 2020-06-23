package com.blocklang.marketplace.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MarketplaceStoreTest {

	private MarketplaceStore store;
	
	@BeforeEach
	public void setup() {
		var rootPath = "c:/";
		var gitRepoUrl = "https://github.com/you/your-repo.git";
		store = new MarketplaceStore(rootPath, gitRepoUrl);
	}
	
	@Test
	public void getLogFile() {
		var logFilePath = store.getLogFilePath();

		assertThat(logFilePath.getParent().compareTo(Path.of("c:/marketplace/github.com/you/your-repo/publishLogs/"))).isEqualTo(0);
		assertThat(logFilePath.getFileName().toString().endsWith(".log")).isTrue();
	}
	
	@Test
	@DisplayName("getLogFile - 两次调用返回同一个日志文件的路径")
	public void getLogFile_call_twice() throws InterruptedException {
		String fileName1 = store.getLogFilePath().getFileName().toString();
		TimeUnit.SECONDS.sleep(1);
		String fileName2 = store.getLogFilePath().getFileName().toString();

		assertThat(fileName1).isEqualTo(fileName2);
	}

	@Test
	public void getRepoSourceDirectory() {
		var repoSourceDirectory = store.getRepoSourceDirectory();
		
		assertThat(repoSourceDirectory.compareTo(Path.of("c:/marketplace/github.com/you/your-repo/source")))
				.isEqualTo(0);
	}
	
	@Test
	public void getPackageVersionDirectory() {
		String version = "1.0.0";
		var packageVersionDirectory = store.getPackageVersionDirectory(version);
		
		assertThat(packageVersionDirectory.compareTo(Path.of("c:/marketplace/github.com/you/your-repo/package/1.0.0/")))
				.isEqualTo(0);
	}
	
	@Test
	public void getRepoBuildDirectory() {
		var repoBuildDirectory = store.getRepoBuildDirectory();
		assertThat(repoBuildDirectory.compareTo(Path.of("c:/marketplace/github.com/you/your-repo/build/")))
				.isEqualTo(0);
	}
	
	@Test
	public void getRepoConfigFile() {
		var repoConfigFile = store.getRepoBlocklangJsonFile();
		assertThat(repoConfigFile.compareTo(Path.of("c:/marketplace/github.com/you/your-repo/source/blocklang.json")))
				.isEqualTo(0);
	}
}
