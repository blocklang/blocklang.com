package com.blocklang.marketplace.runner.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.action.AbstractActionTest;
import com.blocklang.marketplace.apirepo.RepoParser;

public class AbstractParseApiRepoActionTest extends AbstractActionTest{

	@Test
	public void run_success() {
		var action = mock(AbstractParseApiRepoAction.class,
				withSettings().useConstructor(context).defaultAnswer(CALLS_REAL_METHODS));
		var parser = mock(RepoParser.class);
		when(parser.run()).thenReturn(true);
		when(parser.getParsedTags()).thenReturn(Collections.singletonList("1"));
		doReturn(parser).when(action).createApiRepoParser();

		assertThat(action.run()).isTrue();
		assertThat(action.getOutput("parsedTags")).asList().hasSize(1).first().isEqualTo("1");
		assertThat(action.getOutput("not-exist-key")).isNull();
	}
	
	@Test
	public void run_failed() {
		var action = mock(AbstractParseApiRepoAction.class,
				withSettings().useConstructor(context).defaultAnswer(CALLS_REAL_METHODS));
		var parser = mock(RepoParser.class);
		when(parser.run()).thenReturn(false);
		doReturn(parser).when(action).createApiRepoParser();

		assertThat(action.run()).isFalse();
		assertThat(action.getOutput("not-exist-key")).isNull();
	}
	
}
