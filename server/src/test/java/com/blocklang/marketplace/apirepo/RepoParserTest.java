package com.blocklang.marketplace.apirepo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.DefaultExecutionContext;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectFactory;

public class RepoParserTest {
	
	private ApiObjectFactory factory;
	private ExecutionContext context;
	private RepoParser repoParser;
	
	@BeforeEach
	public void setup() {
		factory = mock(ApiObjectFactory.class);
		
		context = new DefaultExecutionContext();
		CliLogger logger = mock(CliLogger.class);
		context.setLogger(logger);
		
		repoParser = mock(RepoParser.class, withSettings().useConstructor(context, factory).defaultAnswer(CALLS_REAL_METHODS));
	}

	@Test
	public void run_success() {
		RefParser tagParser = mock(RefParser.class);
		when(tagParser.run()).thenReturn(ParseResult.SUCCESS, ParseResult.SUCCESS);
		
		doReturn(tagParser).when(repoParser).createTagParser(any(), any(), any());
		
		RefParser masterParser = mock(RefParser.class);
		when(masterParser.run()).thenReturn(ParseResult.SUCCESS);
		
		doReturn(masterParser).when(repoParser).createMasterParser(any(), any(), any());
		
		doReturn(Arrays.asList("0.1.0", "0.2.0")).when(repoParser).getAllTags();
		assertThat(repoParser.run()).isTrue();
		
		List<String> parsedTags = repoParser.getParsedTags();
		assertThat(parsedTags).hasSize(2);
		assertThat(parsedTags.get(0)).isEqualTo("0.1.0");
		assertThat(parsedTags.get(1)).isEqualTo("0.2.0");
	}
	
	@Test
	public void run_tags_is_empty() {
		RefParser tagParser = mock(RefParser.class);
		
		doReturn(tagParser).when(repoParser).createTagParser(any(), any(), any());
		
		RefParser masterParser = mock(RefParser.class);
		when(masterParser.run()).thenReturn(ParseResult.SUCCESS);
		
		doReturn(masterParser).when(repoParser).createMasterParser(any(), any(), any());
		
		doReturn(Collections.emptyList()).when(repoParser).getAllTags();
		assertThat(repoParser.run()).isTrue();
		
		List<String> parsedTags = repoParser.getParsedTags();
		assertThat(parsedTags).isEmpty();
		
		verify(tagParser, never()).run();
		verify(masterParser).run();
	}
	
	@DisplayName("如果一个 tag 解析失败，则不再解析后续 tag 和 master 分支")
	@Test
	public void run_one_of_tag_failed_then_stop_parser() {
		RefParser tagParser = mock(RefParser.class);
		when(tagParser.run()).thenReturn(ParseResult.FAILED, ParseResult.SUCCESS);
		
		doReturn(tagParser).when(repoParser).createTagParser(any(), any(), any());
		
		RefParser masterParser = mock(RefParser.class);
		when(masterParser.run()).thenReturn(ParseResult.SUCCESS);
		
		doReturn(masterParser).when(repoParser).createMasterParser(any(), any(), any());
		
		doReturn(Arrays.asList("0.1.0", "0.2.0")).when(repoParser).getAllTags();
		assertThat(repoParser.run()).isFalse();
		
		verify(tagParser).run();
		verify(masterParser, never()).run();
		
		List<String> parsedTags = repoParser.getParsedTags();
		assertThat(parsedTags).isEmpty();
	}
	
	@Test
	public void run_master_failed_then_stop_parser() {
		RefParser tagParser = mock(RefParser.class);
		when(tagParser.run()).thenReturn(ParseResult.SUCCESS);
		
		doReturn(tagParser).when(repoParser).createTagParser(any(), any(), any());
		
		RefParser masterParser = mock(RefParser.class);
		when(masterParser.run()).thenReturn(ParseResult.FAILED);
		
		doReturn(masterParser).when(repoParser).createMasterParser(any(), any(), any());
		
		doReturn(Arrays.asList("0.1.0")).when(repoParser).getAllTags();
		assertThat(repoParser.run()).isFalse();
		
		verify(tagParser).run();
		verify(masterParser).run();
		
		List<String> parsedTags = repoParser.getParsedTags();
		assertThat(parsedTags).hasSize(1);
		assertThat(parsedTags.get(0)).isEqualTo("0.1.0");
	}
	
	@Test
	public void run_abort_tag_that_parsed() {
		RefParser tagParser = mock(RefParser.class);
		when(tagParser.run()).thenReturn(ParseResult.ABORT, ParseResult.SUCCESS);
		
		doReturn(tagParser).when(repoParser).createTagParser(any(), any(), any());
		
		RefParser masterParser = mock(RefParser.class);
		when(masterParser.run()).thenReturn(ParseResult.SUCCESS);
		
		doReturn(masterParser).when(repoParser).createMasterParser(any(), any(), any());
		
		doReturn(Arrays.asList("0.1.0", "0.2.0")).when(repoParser).getAllTags();
		assertThat(repoParser.run()).isTrue();
		
		List<String> parsedTags = repoParser.getParsedTags();
		assertThat(parsedTags).hasSize(1);
		assertThat(parsedTags.get(0)).isEqualTo("0.2.0");
	}
	
}
