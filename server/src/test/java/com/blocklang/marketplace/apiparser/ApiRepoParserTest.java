package com.blocklang.marketplace.apiparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

public class ApiRepoParserTest {
	
	private AbstractApiRepoParserFactory factory;
	private ExecutionContext context;
	private TagParser tagParser;
	private MasterParser masterParser;
	private ApiRepoParser apiRepoParser;
	
	@BeforeEach
	public void setup() {
		factory = mock(AbstractApiRepoParserFactory.class);
		
		ChangeSetSchemaValidator validator = mock(ChangeSetSchemaValidator.class);
		when(validator.run()).thenReturn(Collections.emptySet());
		when(factory.createSchemaValidator()).thenReturn(validator);
		
		tagParser = mock(TagParser.class);
		when(factory.createTagParser(any())).thenReturn(tagParser);
		
		masterParser = mock(MasterParser.class);
		when(factory.createMasterParser(any())).thenReturn(masterParser);
		
		context = new DefaultExecutionContext();
		CliLogger logger = mock(CliLogger.class);
		context.setLogger(logger);
		
		apiRepoParser = mock(ApiRepoParser.class, withSettings().useConstructor(context, factory).defaultAnswer(CALLS_REAL_METHODS));
	}

	@Test
	public void run_success() {
		when(tagParser.run(eq("0.1.0"))).thenReturn(ParseResult.SUCCESS);
		when(tagParser.run(eq("0.2.0"))).thenReturn(ParseResult.SUCCESS);
		when(masterParser.run()).thenReturn(ParseResult.SUCCESS);
		
		doReturn(Arrays.asList("0.1.0", "0.2.0")).when(apiRepoParser).getAllTags();
		assertThat(apiRepoParser.run()).isTrue();
		
		List<String> parsedTags = apiRepoParser.getParsedTags();
		assertThat(parsedTags).hasSize(2);
		assertThat(parsedTags.get(0)).isEqualTo("0.1.0");
		assertThat(parsedTags.get(1)).isEqualTo("0.2.0");
	}
	
	@Test
	public void run_tags_is_empty() {
		when(masterParser.run()).thenReturn(ParseResult.SUCCESS);
		
		doReturn(Collections.emptyList()).when(apiRepoParser).getAllTags();
		assertThat(apiRepoParser.run()).isTrue();
		
		List<String> parsedTags = apiRepoParser.getParsedTags();
		assertThat(parsedTags).isEmpty();
		
		verify(tagParser, never()).run(anyString());
		verify(masterParser).run();
	}
	
	@DisplayName("如果一个 tag 解析失败，则不再解析后续 tag 和 master 分支")
	@Test
	public void run_one_of_tag_failed_then_stop_parser() {
		when(tagParser.run(eq("0.1.0"))).thenReturn(ParseResult.FAILED);
		
		doReturn(Arrays.asList("0.1.0", "0.2.0")).when(apiRepoParser).getAllTags();
		assertThat(apiRepoParser.run()).isFalse();
		
		verify(tagParser).run(eq("0.1.0"));
		verify(tagParser, never()).run(eq("0.2.0"));
		verify(masterParser, never()).run();
		
		List<String> parsedTags = apiRepoParser.getParsedTags();
		assertThat(parsedTags).isEmpty();
	}
	
	@Test
	public void run_master_failed_then_stop_parser() {
		when(tagParser.run(eq("0.1.0"))).thenReturn(ParseResult.SUCCESS);
		when(masterParser.run()).thenReturn(ParseResult.FAILED);
		
		doReturn(Arrays.asList("0.1.0")).when(apiRepoParser).getAllTags();
		assertThat(apiRepoParser.run()).isFalse();
		
		verify(tagParser).run(eq("0.1.0"));
		verify(masterParser).run();
		
		List<String> parsedTags = apiRepoParser.getParsedTags();
		assertThat(parsedTags).hasSize(1);
		assertThat(parsedTags.get(0)).isEqualTo("0.1.0");
	}
	
	@Test
	public void run_abort_tag_that_parsed() {
		when(tagParser.run(eq("0.1.0"))).thenReturn(ParseResult.ABORT);
		when(tagParser.run(eq("0.2.0"))).thenReturn(ParseResult.SUCCESS);
		when(masterParser.run()).thenReturn(ParseResult.SUCCESS);
		
		doReturn(Arrays.asList("0.1.0", "0.2.0")).when(apiRepoParser).getAllTags();
		assertThat(apiRepoParser.run()).isTrue();
		
		List<String> parsedTags = apiRepoParser.getParsedTags();
		assertThat(parsedTags).hasSize(1);
		assertThat(parsedTags.get(0)).isEqualTo("0.2.0");
	}
	
}
