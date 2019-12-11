package com.blocklang.core.test;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import com.blocklang.BlockLangApplication;
import com.blocklang.listener.BlockLangRunner;

@SpringBootTest(classes = BlockLangApplication.class)
@Transactional
public class AbstractServiceTest {

	// 当运行 service 测试用例时，都会运行 BlockLangRunner，此处改为使用 mock 的 BlockLangRunner。
	@MockBean
	private BlockLangRunner blockLangRunner;
	
	@Autowired
	protected JdbcTemplate jdbcTemplate;

	protected int countRowsInTable(String tableName) {
		return JdbcTestUtils.countRowsInTable(jdbcTemplate, tableName);
	}

}
