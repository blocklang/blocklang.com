package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class UrlUtilTest {

	@Test
	public void testTrimHttpInUrl() {
		assertThat(UrlUtil.trimHttpInUrl(null)).isEmpty();
		assertThat(UrlUtil.trimHttpInUrl("")).isEmpty();
		assertThat(UrlUtil.trimHttpInUrl("http://x")).isEqualTo("//x");
		assertThat(UrlUtil.trimHttpInUrl("https://x")).isEqualTo("https://x");
	}
}
