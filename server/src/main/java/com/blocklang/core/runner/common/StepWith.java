package com.blocklang.core.runner.common;

public class StepWith {

	private String key;
	private String expression;
	private Object value;

	public StepWith(String key, String expression) {
		this.key = key;
		this.expression = expression;
		// value 的值默认等于 expression
		this.value = expression;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
