package com.blocklang.marketplace.service;

import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.webapi.data.JsObjectData;

public interface JsObjectApiRefService {

	void save(RefData<JsObjectData> refData);
}
