package com.blocklang.core.runner.action;

import static org.mockito.Mockito.mock;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.DefaultExecutionContext;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;

public class AbstractActionTest {
	
	ExecutionContext context;

	@BeforeEach
	public void setup(@TempDir Path tempDir) {
		context = new DefaultExecutionContext();
		var logger = mock(CliLogger.class);
		context.setLogger(logger);
		
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		var gitUrl = "https://github.com/you/your-repo.git";
		task.setGitUrl(gitUrl);
		MarketplaceStore store = new MarketplaceStore(tempDir.toString(), gitUrl);
		
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		context.putValue(ExecutionContext.PUBLISH_TASK, task);

	}

}