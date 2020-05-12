package com.blocklang.core.runner.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.DefaultExecutionContext;
import com.blocklang.core.runner.common.ExecutionContext;

public class CheckGitUrlActionTest {

	private ExecutionContext context;
	
	@BeforeEach
	public void setup() {
		context = new DefaultExecutionContext();
		var logger = mock(CliLogger.class);
		context.setLogger(logger);
	}
	
	@Test
	public void new_should_set_inputs() {
		assertThrows(IllegalArgumentException.class,  () -> new CheckGitUrlAction(context));
	}
	
	@Test
	public void run_failed() {
		// 将 GIT_URL 提升为全局变量，而不是 Action 一级的变量
		context.putValue(CheckGitUrlAction.INPUT_GIT_URL, "https://not-exist-host/you/you-repo.git");
		
		var action = new CheckGitUrlAction(context);
		assertThat(action.run()).isEmpty();
	}
	
	@Test
	public void run_success() {
		context.putValue(CheckGitUrlAction.INPUT_GIT_URL, "https://github.com/blocklang/blocklang.com.git");
		
		var action = new CheckGitUrlAction(context);
		assertThat(action.run()).isPresent();
	}
}
