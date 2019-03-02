package com.blocklang.develop.constant;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class AppTypeTest {

	@Test
	public void from_key() {
		assertThat(AppType.fromKey("01").getValue()).isEqualTo("Web");
	}
	
	@Test
	public void from_value() {
		assertThat(AppType.fromValue("Web").getKey()).isEqualTo("01");
	}
	
}
