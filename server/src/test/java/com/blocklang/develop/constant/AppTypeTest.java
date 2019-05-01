package com.blocklang.develop.constant;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class AppTypeTest {

	@Test
	public void from_key() {
		assertThat(AppType.fromKey("01").getValue()).isEqualTo("web");
	}
	
	@Test
	public void from_value() {
		assertThat(AppType.fromValue("web").getKey()).isEqualTo("01");
		assertThat(AppType.fromValue("Web").getKey()).isEqualTo("01");
	}
	
}
