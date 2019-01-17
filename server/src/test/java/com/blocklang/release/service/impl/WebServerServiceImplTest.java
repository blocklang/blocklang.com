package com.blocklang.release.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.OsType;
import com.blocklang.release.dao.WebServerDao;
import com.blocklang.release.model.WebServer;
import com.blocklang.release.service.AbstractServiceTest;
import com.blocklang.release.service.WebServerService;

public class WebServerServiceImplTest extends AbstractServiceTest{

	@Autowired
	private WebServerService webServerService;
	@Autowired
	private WebServerDao webServerDao;
	
	@Test
	public void find_by_id_no_data() {
		Optional<WebServer> webServerOption = webServerService.findById(1);
		assertThat(webServerOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_by_id_success() {
		WebServer webServer = new WebServer();
		webServer.setArch(Arch.UNKNOWN);
		webServer.setIp("ip");
		webServer.setOsType(OsType.UNKNOWN);
		webServer.setOsVersion("v1");
		webServer.setServerToken("server_token");
		webServer.setCreateUserId(1);
		webServer.setCreateTime(LocalDateTime.now());
		
		webServerDao.save(webServer);
		
		Optional<WebServer> webServerOption = webServerService.findById(webServer.getId());
		assertThat(webServerOption.isPresent(), is(true));
	}
}
