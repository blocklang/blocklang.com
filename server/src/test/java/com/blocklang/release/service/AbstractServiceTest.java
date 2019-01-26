package com.blocklang.release.service;

import javax.transaction.Transactional;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

import com.blocklang.BlockLangApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BlockLangApplication.class)
@Transactional
public class AbstractServiceTest {
	@Autowired
	protected JdbcTemplate jdbcTemplate;

	protected int countRowsInTable(String tableName) {
		return JdbcTestUtils.countRowsInTable(jdbcTemplate, tableName);
	}

}
