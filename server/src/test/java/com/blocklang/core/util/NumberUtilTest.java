package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class NumberUtilTest {

	@Test
	public void to_int() {
		assertThat(NumberUtil.toInt("1").get()).isEqualTo(1);
		assertThat(NumberUtil.toInt("a")).isEmpty();
	}
}
