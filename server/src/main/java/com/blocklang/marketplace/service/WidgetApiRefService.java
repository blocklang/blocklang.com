package com.blocklang.marketplace.service;

import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.widget.data.WidgetData;

public interface WidgetApiRefService {

	void save(RefData<WidgetData> refData);
	
}
