package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.release.model.WebServer;

public interface WebServerService {

	Optional<WebServer> findById(Integer webServerId);

}
