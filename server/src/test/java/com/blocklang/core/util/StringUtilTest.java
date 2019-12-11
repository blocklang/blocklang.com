package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

	@Test
	public void byte_length() {
		assertThat(StringUtil.byteLength(null)).isEqualTo(0);
		assertThat(StringUtil.byteLength("a")).isEqualTo(1);
		assertThat(StringUtil.byteLength("中")).isEqualTo(2);
	}
}
