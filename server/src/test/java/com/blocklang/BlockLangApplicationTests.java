package com.blocklang;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.listener.BlockLangRunner;

@SpringBootTest
public class BlockLangApplicationTests {

	@MockBean
	private BlockLangRunner blocklangRunner;
	
	@Test
	public void contextLoads() {
	}

}

