package com.blocklang.marketplace.apirepo.schema;

import java.util.ArrayList;
import java.util.List;

import com.blocklang.marketplace.apirepo.ChangedObject;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 自定义数据类型
 * 
 * @author Zhengwei Jin
 *
 */
public class ApiSchemaData implements ChangedObject{

	private String id;
	private String code;
	private String type;
	private String name;
	// 因为 default 和 enum 是关键字，不能作为属性名
	@JsonProperty("default")
	private String defaultValue;
	@JsonProperty("enum")
	private List<String> options;
	private String description;
	private List<ApiSchemaData> properties = new ArrayList<ApiSchemaData>();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<ApiSchemaData> getProperties() {
		return properties;
	}

	public void setProperties(List<ApiSchemaData> properties) {
		this.properties = properties;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
