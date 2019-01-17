package com.blocklang.release.util;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;

public class IdGeneratorTest {

	@Test
	public void short_uuid_22_characters() {
		String uuid = IdGenerator.shortUuid();
		assertThat(uuid.length() <= 22, is(true));
	}
	
	@Test
	public void uuid_32_characters() {
		assertThat(IdGenerator.uuid().length(), is(32));
	}
}
