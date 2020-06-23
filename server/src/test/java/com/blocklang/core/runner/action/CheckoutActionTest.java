package com.blocklang.core.runner.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CheckoutActionTest extends AbstractActionTest {

	private CheckoutAction action;
	
	@BeforeEach
	public void setup() {
		action = spy(new CheckoutAction(context));
	}
	
	@Test
	public void run_success() {
		// 注意，给 syncRepository 方法加上 final 后，该行代码不会生效
		doReturn(true).when(action).isValidRemoteRepository();
		doNothing().when(action).syncRepository();
		assertThat(action.run()).isTrue();
	}
	
	@Test
	public void run_invalid_git_url() {
		doReturn(false).when(action).isValidRemoteRepository();
		assertThat(action.run()).isFalse();
	}
	
	@Test
	public void run_sync_throw_errors() {
		doReturn(true).when(action).isValidRemoteRepository();
		doThrow(RuntimeException.class).when(action).syncRepository();
		assertThat(action.run()).isFalse();
	}
	
}
