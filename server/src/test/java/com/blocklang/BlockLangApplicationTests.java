package com.blocklang;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.blocklang.listener.BlockLangRunner;

@ActiveProfiles("test")
@SpringBootTest
public class BlockLangApplicationTests {

	@MockBean
	private BlockLangRunner blocklangRunner;
	
	@Test
	public void contextLoads() {
	}

}

