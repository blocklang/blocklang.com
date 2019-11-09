package com.blocklang.develop.designer.data;

public class AttachedWidgetProperty {

	// 以下字段是部件属性的基本信息
	private String code;
	private String name;
	private String valueType;
	
	// 以下字段是实例化部件后，需要为属性设置的值
	private String id;
	private String value;
	private Boolean expr = false;
	

	/**
	 * 将部件添加到页面中后，为属性生成的 id。不是部件属性基本信息的 id。
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * 将部件添加到页面中后，为属性生成的 id。不是部件属性基本信息的 id。
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public Boolean isExpr() {
		return expr;
	}

	public void setExpr(Boolean expr) {
		this.expr = expr;
	}

}
