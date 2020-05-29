package com.blocklang.marketplace.apiparser;

/**
 * 应用日志变更
 * 
 * @author Zhengwei Jin
 *
 */
public interface WidgetOperator<T> {
	
	/**
	 * 将增量变更应用到 widget 上
	 * 
	 * @param context
	 * @param data
	 * @return 如果应用成功，返回 <code>true</code>；否则返回 <code>false</code>
	 */
	public boolean apply(WidgetOperatorContext context, T data);
}
