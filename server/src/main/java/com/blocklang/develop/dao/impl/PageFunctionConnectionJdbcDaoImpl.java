package com.blocklang.develop.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.blocklang.develop.dao.PageFunctionConnectionJdbcDao;
import com.blocklang.develop.model.PageFunctionConnection;

@Repository
public class PageFunctionConnectionJdbcDaoImpl implements PageFunctionConnectionJdbcDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private static final String SQL_INSERT_PAGE_FUNC_CONNECTION = "INSERT INTO " +
			"page_func_connection " +
			"(dbid, " +
			"project_resource_id, " +
			"page_func_id, " +
			"from_node_id, " +
			"from_output_port_id, " +
			"to_node_id, " +
			"to_input_port_id) " +
			"VALUES (?,?,?,?,?,?)";

	@Override
	public void batchSave(List<PageFunctionConnection> connections) {
		jdbcTemplate.batchUpdate(SQL_INSERT_PAGE_FUNC_CONNECTION, new BatchPreparedStatementSetter() {
			
			@Override
			public int getBatchSize() {
				return connections.size();
			}

			@Override
			public void setValues(PreparedStatement ps, int index) throws SQLException {
				PageFunctionConnection each = connections.get(index);
				ps.setString(1, each.getId());
				ps.setInt(2, each.getPageId());
				ps.setString(3, each.getFunctionId());
				ps.setString(4, each.getFromNodeId());
				ps.setString(5, each.getFromOutputPortId());
				ps.setString(6, each.getToNodeId());
				ps.setString(7, each.getToInputPortId());
			}
			
		});
	}

	private static final String SQL_DELETE_PAGE_FUNC_CONNECTION_BY_PAGE_ID = "DELETE FROM " +
			"page_func_connection " +
			"WHERE " +
			"project_resource_id=?";
	
	@Override
	public void deleteByPageId(Integer pageId) {
		jdbcTemplate.update(SQL_DELETE_PAGE_FUNC_CONNECTION_BY_PAGE_ID, pageId);
	}
	
}
