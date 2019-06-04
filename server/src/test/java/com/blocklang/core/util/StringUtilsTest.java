package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void byte_length() {
		assertThat(StringUtils.byteLength(null)).isEqualTo(0);
		assertThat(StringUtils.byteLength("a")).isEqualTo(1);
		assertThat(StringUtils.byteLength("中")).isEqualTo(2);
	}
}
