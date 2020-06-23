package com.blocklang.marketplace.apirepo;

public abstract class Change {

	public abstract void setData(ChangeData data);
	
	/**
	 * 将增量变更应用到 Api Object 上
	 * 
	 * @param context
	 * @return 如果应用成功，返回 <code>true</code>；否则返回 <code>false</code>
	 */
	public boolean apply(ApiObjectContext context) {
		return false;
	}

}
