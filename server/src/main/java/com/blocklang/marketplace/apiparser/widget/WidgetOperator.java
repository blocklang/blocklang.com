package com.blocklang.marketplace.apiparser.widget;

import com.blocklang.marketplace.apiparser.ChangeData;

/**
 * 应用日志变更
 * 
 * @author Zhengwei Jin
 *
 */
public interface WidgetOperator {
	
	public void setData(ChangeData data);
	
	/**
	 * 将增量变更应用到 widget 上
	 * 
	 * @param context
	 * @return 如果应用成功，返回 <code>true</code>；否则返回 <code>false</code>
	 */
	public boolean apply(WidgetOperatorContext context);
}
