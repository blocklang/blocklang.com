package com.blocklang.marketplace.apiparser;

public interface Operator<T extends Codeable> {
	
	public void setData(ChangeData data);

	/**
	 * 将增量变更应用到组件上
	 * 
	 * @param context
	 * @return 如果应用成功，返回 <code>true</code>；否则返回 <code>false</code>
	 */
	public boolean apply(OperatorContext<T> context);
}
