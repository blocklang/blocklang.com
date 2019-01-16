package com.blocklang.release.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.WebServer;

public interface WebServerDao extends JpaRepository<WebServer, Integer>{

	Optional<WebServer> findByServerToken(String serverToken);

}
