package com.blocklang.release.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.release.dao.WebServerDao;
import com.blocklang.release.model.WebServer;
import com.blocklang.release.service.WebServerService;

@Service
public class WebServerServiceImpl implements WebServerService {

	@Autowired
	private WebServerDao webServerDao;
	
	@Override
	public Optional<WebServer> findById(Integer webServerId) {
		return webServerDao.findById(webServerId);
	}

}
