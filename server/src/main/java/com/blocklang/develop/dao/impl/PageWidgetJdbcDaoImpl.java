package com.blocklang.develop.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.blocklang.develop.dao.PageWidgetJdbcDao;
import com.blocklang.develop.designer.data.AttachedWidget;
import com.blocklang.develop.model.PageWidgetAttrValue;

@Repository
public class PageWidgetJdbcDaoImpl implements PageWidgetJdbcDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	// 当 Text Block 发布后再支持
//	private static final String SQL_INSERT_PAGE_WIDGET = """
//			INSERT INTO
//			page_widget
//			(dbid,
//			project_resource_id,
//			api_repo_id,
//			widget_code,
//			parent_id,
//			seq)
//			VALUES (?,?,?,?,?,?)""";
	private static final String SQL_INSERT_PAGE_WIDGET = "INSERT INTO "+
			"page_widget "+
			"(dbid, "+
			"project_resource_id, "+
			"api_repo_id, "+
			"widget_code, "+
			"parent_id, "+
			"seq) "+
			"VALUES (?,?,?,?,?,?)";

	@Override
	public void batchSaveWidgets(Integer pageId, List<AttachedWidget> widgets) {
		jdbcTemplate.batchUpdate(SQL_INSERT_PAGE_WIDGET, new BatchPreparedStatementSetter() {

			@Override
			public int getBatchSize() {
				return widgets.size();
			}

			@Override
			public void setValues(PreparedStatement ps, int index)
					throws SQLException {
				AttachedWidget each = widgets.get(index);
				ps.setString(1, each.getId());
				ps.setInt(2, pageId);
				ps.setInt(3, each.getApiRepoId());
				ps.setString(4, each.getWidgetCode());
				ps.setString(5, each.getParentId());
				// seq 是从1开始的，是全页面内排序
				ps.setInt(6, index+1);
			}
			
		});
	}

//	private static final String SQL_INSERT_PAGE_WIDGET_ATTR_VALUE = """
//			INSERT INTO
//			page_widget_attr_value
//			(dbid,
//			page_widget_id,
//			widget_attr_code,
//			attr_value,
//			is_expr)
//			VALUES (?,?,?,?,?)""";
	private static final String SQL_INSERT_PAGE_WIDGET_ATTR_VALUE = "INSERT INTO "+
			"page_widget_attr_value "+
			"(dbid, "+
			"page_widget_id, "+
			"widget_attr_code, "+
			"attr_value, "+
			"is_expr) "+
			"VALUES (?,?,?,?,?)";

	@Override
	public void batchSaveWidgetProperties(List<PageWidgetAttrValue> properties) {
		jdbcTemplate.batchUpdate(SQL_INSERT_PAGE_WIDGET_ATTR_VALUE, new BatchPreparedStatementSetter() {

			@Override
			public int getBatchSize() {
				return properties.size();
			}

			@Override
			public void setValues(PreparedStatement ps, int index)
					throws SQLException {
				PageWidgetAttrValue each = properties.get(index);
				ps.setString(1, each.getId());
				ps.setString(2, each.getPageWidgetId());
				ps.setString(3, each.getWidgetAttrCode());
				ps.setString(4, each.getAttrValue());
				ps.setBoolean(5, each.isExpr());
			}
			
		});
	}

//	private static final String SQL_DELETE_PAGE_WIDGET_ATTR_VALUE = """
//			DELETE FROM
//			page_widget_attr_value
//			WHERE
//			page_widget_id
//			IN
//			(SELECT dbid FROM PAGE_WIDGET WHERE project_resource_id=?)""";
	
	private static final String SQL_DELETE_PAGE_WIDGET_ATTR_VALUE = "DELETE FROM "+
			"page_widget_attr_value "+
			"WHERE "+
			"page_widget_id "+
			"IN "+
			"(SELECT dbid FROM PAGE_WIDGET WHERE project_resource_id=?)";
	
	@Override
	public void deleteWidgetProperties(Integer pageId) {
		jdbcTemplate.update(SQL_DELETE_PAGE_WIDGET_ATTR_VALUE, pageId);
	}

//	private static final String SQL_DELETE_PAGE_WIDGET = """
//			DELETE FROM
//			page_widget
//			WHERE
//			project_resource_id=?""";
	
	private static final String SQL_DELETE_PAGE_WIDGET = "DELETE FROM "+
			"page_widget "+
			"WHERE "+
			"project_resource_id=?";
	@Override
	public void deleteWidgets(Integer pageId) {
		jdbcTemplate.update(SQL_DELETE_PAGE_WIDGET, pageId);
	}

	
}
