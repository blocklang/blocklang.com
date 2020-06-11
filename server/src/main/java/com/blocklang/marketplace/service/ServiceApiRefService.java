package com.blocklang.marketplace.service;

import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.service.data.ServiceData;

public interface ServiceApiRefService {

	void save(RefData<ServiceData> refData);
	
}
