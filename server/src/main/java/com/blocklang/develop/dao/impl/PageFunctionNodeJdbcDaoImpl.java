package com.blocklang.develop.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.blocklang.develop.dao.PageFunctionNodeJdbcDao;
import com.blocklang.develop.model.PageFunctionNode;

@Repository
public class PageFunctionNodeJdbcDaoImpl implements PageFunctionNodeJdbcDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private static final String SQL_INSERT_PAGE_FUNC_NODE = "INSERT INTO "+
			"page_func_node "+
			"(dbid, "+
			"project_resource_id, " +
			"page_func_id, "+
			"\"left\", " + // 在 postgresql 中如果不加双引号，会报语法错误
			"top, " +
			"layout, " +
			"category) " +
//			"bind_source, " +
//			"api_repo_id, " +
//			"code) "+
			"VALUES (?,?,?,?,?,?,?)";//,?,?,?
	
	@Override
	public void batchSave(List<PageFunctionNode> nodes) {
		jdbcTemplate.batchUpdate(SQL_INSERT_PAGE_FUNC_NODE, new BatchPreparedStatementSetter() {

			@Override
			public int getBatchSize() {
				return nodes.size();
			}

			@Override
			public void setValues(PreparedStatement ps, int index)
					throws SQLException {
				PageFunctionNode each = nodes.get(index);
				ps.setString(1, each.getId());
				ps.setInt(2, each.getPageId());
				ps.setString(3, each.getFunctionId());
				ps.setInt(4, each.getLeft());
				ps.setInt(5, each.getTop());
				ps.setString(6, each.getLayout().getKey());
				ps.setString(7, each.getCategory().getKey());
//				ps.setString(8, each.getBindSource().getKey());
//				ps.setInt(9, each.getApiRepoId());
//				ps.setString(10, each.getCode());
			}
			
		});
	}

	private static final String SQL_DELETE_PAGE_FUNC_NODE_BY_PAGE_ID = "DELETE FROM " +
			"page_func_node " +
			"WHERE " +
			"project_resource_id=?";
	
	@Override
	public void deleteByPageId(Integer pageId) {
		jdbcTemplate.update(SQL_DELETE_PAGE_FUNC_NODE_BY_PAGE_ID, pageId);
	}
}
