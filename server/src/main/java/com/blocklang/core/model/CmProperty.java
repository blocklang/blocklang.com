package com.blocklang.core.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.blocklang.core.constant.DataType;
import com.blocklang.core.constant.converter.DataTypeConverter;

@Entity
@Table(name = "cm_property")
public class CmProperty extends PartialIdField{

	private static final long serialVersionUID = 5003502231471490342L;

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
	private Integer parentId = -1;

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
