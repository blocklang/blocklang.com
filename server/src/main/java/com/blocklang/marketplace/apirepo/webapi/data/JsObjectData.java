package com.blocklang.marketplace.apirepo.webapi.data;

import java.util.ArrayList;
import java.util.List;

import com.blocklang.marketplace.apirepo.ApiObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class JsObjectData implements ApiObject {

	private String id;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
