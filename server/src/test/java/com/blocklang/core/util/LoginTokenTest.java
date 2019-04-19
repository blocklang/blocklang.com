package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LoginTokenTest {

	@Test
	public void encode_then_decode() {
		LoginToken loginToken = new LoginToken();
		String encoded = loginToken.encode("qq", "token");

		assertThat(encoded).isNotBlank();
		assertThat(encoded).doesNotContain(":");
		
		loginToken = new LoginToken();
		loginToken.decode(encoded);
		assertThat(loginToken.getProvider()).isEqualTo("qq");
		assertThat(loginToken.getToken()).isEqualTo("token");
	}
}
