package com.blocklang.marketplace.apiparser.webapi;

import java.util.ArrayList;
import java.util.List;

import com.blocklang.marketplace.apiparser.ChangeData;
import com.blocklang.marketplace.apiparser.Codeable;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class JsObjectData implements ChangeData, Codeable {

	private String code;
	private String name;
	private String description;
	private List<JsFunction> functions = new ArrayList<JsFunction>();

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

	public List<JsFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<JsFunction> functions) {
		this.functions = functions;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * 获取函数编码
	 * 
	 * @return 返回最大编码，如果还未包含函数则返回 "0"
	 */
	@JsonIgnore
	public String getMaxFunctionCode() {
		String maxSeed = "0";
		if(!functions.isEmpty()) {
			maxSeed = functions.get(functions.size() - 1).getCode();
		}
		return maxSeed;
	}

}
