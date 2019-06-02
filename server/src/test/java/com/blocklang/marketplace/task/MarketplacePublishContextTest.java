package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class MarketplacePublishContextTest {

	private MarketplacePublishContext context;
	
	@Before
	public void setUp() {
		context = new MarketplacePublishContext(
				"c:/blocklang", 
				"github.com",
				"jack", 
				"app");
	}
	
	@Test
	public void get_repo_source_directory() {
		assertThat(context.getRepoSourceDirectory().compareTo(Paths.get("c:/blocklang/marketplace/github.com/jack/app/source"))).isEqualTo(0);
	}
	
	@Test
	public void get_repo_publish_log_directory() {
		assertThat(context.getRepoPublishLogDirectory().compareTo(Paths.get("c:/blocklang/marketplace/github.com/jack/app/publishLogs"))).isEqualTo(0);
	}
}
