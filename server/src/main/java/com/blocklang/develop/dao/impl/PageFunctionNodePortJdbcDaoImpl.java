package com.blocklang.develop.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.blocklang.develop.dao.PageFunctionNodePortJdbcDao;
import com.blocklang.develop.model.PageFunctionNodePort;

@Repository
public class PageFunctionNodePortJdbcDaoImpl implements PageFunctionNodePortJdbcDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private static final String SQL_INSERT_PAGE_FUNC_NODE_PORT = "INSERT INTO " +
			"page_func_node_port " +
			"(dbid, " +
			"project_resource_id, " +
			"page_func_node_id, " +
			"port_type, " +
			"flow_type, " +
			"output_sequence_port_text, " + 
			"input_data_port_value) " +
			"VALUES (?,?,?,?,?,?,?)";
	
	@Override
	public void batchSave(List<PageFunctionNodePort> ports) {
		jdbcTemplate.batchUpdate(SQL_INSERT_PAGE_FUNC_NODE_PORT, new BatchPreparedStatementSetter() {
			
			@Override
			public int getBatchSize() {
				return ports.size();
			}

			@Override
			public void setValues(PreparedStatement ps, int index) throws SQLException {
				PageFunctionNodePort each = ports.get(index);
				ps.setString(1, each.getId());
				ps.setInt(2, each.getProjectResourceId());
				ps.setString(3, each.getNodeId());
				ps.setString(4, each.getPortType().getKey());
				ps.setString(5, each.getFlowType().getKey());
				ps.setString(6, each.getOutputSequencePortText());
				ps.setString(7, each.getInputDataPortValue());
			}
			
		});
		
	}

	private static final String SQL_DELETE_PAGE_FUNC_NODE_PORT_BY_PAGE_ID = "DELETE FROM " +
			"page_func_node_port " +
			"WHERE " +
			"project_resource_id=?";
	
	@Override
	public void deleteByPageId(Integer pageId) {
		jdbcTemplate.update(SQL_DELETE_PAGE_FUNC_NODE_PORT_BY_PAGE_ID);
	}

}
