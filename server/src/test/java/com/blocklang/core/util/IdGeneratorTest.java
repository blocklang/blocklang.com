package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class IdGeneratorTest {

	@Test
	public void short_uuid_22_characters() {
		String uuid = IdGenerator.shortUuid();
		assertThat(uuid.length()).isLessThanOrEqualTo(22);
	}
	
	@Test
	public void uuid_32_characters() {
		assertThat(IdGenerator.uuid().length()).isEqualTo(32);
	}
}
