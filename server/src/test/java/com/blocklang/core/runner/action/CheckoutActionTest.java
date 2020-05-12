package com.blocklang.core.runner.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.DefaultExecutionContext;
import com.blocklang.core.runner.common.ExecutionContext;

public class CheckoutActionTest {

	private ExecutionContext context;
	
	@BeforeEach
	public void setup() {
		context = new DefaultExecutionContext();
		var logger = mock(CliLogger.class);
		context.setLogger(logger);
	}
	
	@Test
	public void new_should_set_inputs() {
		assertThrows(IllegalArgumentException.class, () -> new CheckoutAction(context));
		
		context.putValue(CheckoutAction.INPUT_REPOSITORY, "a repo url");
		assertThrows(IllegalArgumentException.class, () -> new CheckoutAction(context));
	}
	
	// 因为目前尚不了解如何在测试用例中模拟下载远程仓库，所以未添加 run_success 测试用例
	
	@Test
	public void run_failed(@TempDir Path tempDir) {
		context.putValue(CheckoutAction.INPUT_REPOSITORY, "a repo url");
		context.putValue(CheckoutAction.INPUT_LOCAL_SOURCE_DIRECTORY, tempDir);
		
		var action = new CheckoutAction(context);
		assertThat(action.run()).isEmpty();
	}
	
}
