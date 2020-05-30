package com.blocklang.marketplace.apiparser;

import java.util.List;

import com.blocklang.marketplace.apiparser.widget.WidgetOperator;
import com.blocklang.marketplace.apiparser.widget.WidgetOperatorContext;

// 应用一个 changelog 文件中的所有 change
@Deprecated
public class WidgetMerger {
	public WidgetMerger() {
	}
	
	public boolean apply(WidgetOperatorContext context, List<WidgetOperator> changes) {
		boolean anyOperatorsInvalid = false;
		
		for (WidgetOperator change : changes) {
			anyOperatorsInvalid = !change.apply(context);
		}
		
		return !anyOperatorsInvalid;
	}
	
	
}
