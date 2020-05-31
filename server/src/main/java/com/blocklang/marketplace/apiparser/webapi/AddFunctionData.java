package com.blocklang.marketplace.apiparser.webapi;

import java.util.List;

import com.blocklang.marketplace.apiparser.ChangeData;

public class AddFunctionData implements ChangeData {

	private List<JsFunction> functions;

	public List<JsFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<JsFunction> functions) {
		this.functions = functions;
	}

}
