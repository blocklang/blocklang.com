package com.blocklang.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.constant.DataType;
import com.blocklang.core.constant.converter.DataTypeConverter;

@Entity
@Table(name = "cm_property")
public class CmProperty implements Serializable{
	
	private static final long serialVersionUID = -3755026562221650543L;

	// 该表中的数据，都是在开发阶段设置的，不需要后期维护，所以不需要设置为自动增长
	@Id
	@Column(name = "dbid", updatable = false)
	private Integer id;
	
	@Column(name = "prop_key", nullable = false, length = 32)
	private String key;
	
	@Column(name = "prop_value", nullable = false, length = 128)
	private String value;
	
	@Column(name = "prop_desc", length = 32)
	private String description;
	
	@Convert(converter = DataTypeConverter.class)
	@Column(name = "data_type", nullable = false, length = 2)
	private DataType dataType;
	
	@Column(name = "is_valid", nullable = false)
	private boolean valid = true;

	@Column(name = "parent_id", nullable = false)
	private Integer parentId = Constant.TREE_ROOT_ID;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	
}
