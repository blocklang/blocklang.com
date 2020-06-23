package com.blocklang.marketplace.data.changelog;

import com.blocklang.marketplace.apirepo.ChangeData;

/**
 * @deprecated
 * @author 金正伟
 *
 */
public class Change {
	private String operator;
	private ChangeData data;

	public Change(String operator, ChangeData data) {
		super();
		this.operator = operator;
		this.data = data;
	}

	public String getOperator() {
		return operator;
	}

	public <T extends ChangeData> T getData(Class<T> clazz) {
		return clazz.cast(data);
	}

}
