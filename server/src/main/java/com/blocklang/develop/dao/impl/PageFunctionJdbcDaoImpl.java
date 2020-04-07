package com.blocklang.develop.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.blocklang.develop.dao.PageFunctionJdbcDao;
import com.blocklang.develop.model.PageFunction;

@Repository
public class PageFunctionJdbcDaoImpl implements PageFunctionJdbcDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private static final String SQL_INSERT_PAGE_FUNC = "INSERT INTO "+
			"page_func "+
			"(dbid, "+
			"project_resource_id) "+
			"VALUES (?,?)";
	
	@Override
	public void batchSave(List<PageFunction> pageFunctions) {
		jdbcTemplate.batchUpdate(SQL_INSERT_PAGE_FUNC, new BatchPreparedStatementSetter() {

			@Override
			public int getBatchSize() {
				return pageFunctions.size();
			}

			@Override
			public void setValues(PreparedStatement ps, int index)
					throws SQLException {
				PageFunction each = pageFunctions.get(index);
				ps.setString(1, each.getId());
				ps.setInt(2, each.getPageId());
			}
			
		});
	}

	private static final String SQL_DELETE_PAGE_FUNC_BY_PAGE_ID = "DELETE FROM " +
			"page_func " +
			"WHERE " +
			"project_resource_id=?";
	@Override
	public void deleteByPageId(Integer pageId) {
		jdbcTemplate.update(SQL_DELETE_PAGE_FUNC_BY_PAGE_ID, pageId);
	}

}
