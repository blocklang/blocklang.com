package com.blocklang.core.runner.action;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.DefaultExecutionContext;
import com.blocklang.core.runner.common.ExecutionContext;

public class ListGitTagsActionTest {

	private ExecutionContext context;
	
	@BeforeEach
	public void setup() {
		context = new DefaultExecutionContext();
		var logger = mock(CliLogger.class);
		context.setLogger(logger);
	}
	
	@Test
	public void new_should_set_inputs() {
		assertThrows(IllegalArgumentException.class, () -> new ListGitTagsAction(context));
	}
	
	@Test
	public void run_local_source_directory_is_invalid_git_repo(@TempDir Path tempDir) {
		context.putValue(ListGitTagsAction.INPUT_LOCAL_SOURCE_DIRECTORY, tempDir);
		
		var action = new ListGitTagsAction(context);
		
		assertThrows(IllegalArgumentException.class, () -> action.run());
	}
}
