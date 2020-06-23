package com.blocklang.marketplace.apirepo.webapi.data;

import java.util.List;

import com.blocklang.marketplace.apirepo.ChangeData;

public class AddFunctionData implements ChangeData {

	private List<JsFunction> functions;

	public List<JsFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<JsFunction> functions) {
		this.functions = functions;
	}

}
