package com.blocklang.develop.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.blocklang.develop.dao.PageDataJdbcDao;
import com.blocklang.develop.model.PageDataItem;

@Repository
public class PageDataJdbcDaoImpl implements PageDataJdbcDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	//	private static final String SQL_DELETE_PAGE_DATA = """
	//	DELETE FROM
	//	page_data
	//	WHERE
	//	project_resource_id=?""";
	private static final String SQL_DELETE_PAGE_DATA = "DELETE FROM "+
		"page_data "+
		"WHERE "+
		"project_resource_id=?";
	
	@Override
	public void delete(Integer pageId) {
		jdbcTemplate.update(SQL_DELETE_PAGE_DATA, pageId);
	}

	//	private static final String SQL_INSERT_PAGE_DATA = """
	//	INSERT INTO
	//	page_data
	//	(dbid,
	//	project_resource_id,
	//	name,
	//	type,
	//	default_value,
	//	parent_id,
	//	seq)
	//	VALUES (?,?,?,?,?,?,?)""";
	private static final String SQL_INSERT_PAGE_DATA = "INSERT INTO "+
		"page_data "+
		"(dbid, "+
		"project_resource_id, "+
		"name, "+
		"type, "+
		"default_value, " +
		"parent_id, "+
		"seq) "+
		"VALUES (?,?,?,?,?,?,?)";

	@Override
	public void batchSave(Integer pageId, List<PageDataItem> allData) {
		jdbcTemplate.batchUpdate(SQL_INSERT_PAGE_DATA, new BatchPreparedStatementSetter() {

			@Override
			public int getBatchSize() {
				return allData.size();
			}

			@Override
			public void setValues(PreparedStatement ps, int index)
					throws SQLException {
				PageDataItem each = allData.get(index);
				ps.setString(1, each.getId());
				ps.setInt(2, pageId);
				ps.setString(3, each.getName());
				ps.setString(4, each.getType());
				ps.setString(5, each.getValue());
				ps.setString(6, each.getParentId());
				// seq 是从1开始的，是全页面内排序
				ps.setInt(7, index+1);
			}
			
		});

	}

}
