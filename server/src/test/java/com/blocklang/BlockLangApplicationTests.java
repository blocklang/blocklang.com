package com.blocklang;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.test.AbstractSpringTest;
import com.blocklang.listener.BlockLangRunner;

@SpringBootTest
public class BlockLangApplicationTests extends AbstractSpringTest{

	@MockBean
	private BlockLangRunner blocklangRunner;
	
	@Test
	public void contextLoads() {
	}

}

