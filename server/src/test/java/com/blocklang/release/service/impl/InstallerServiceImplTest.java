package com.blocklang.release.service.impl;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.release.data.RegistrationInfo;
import com.blocklang.release.service.AbstractServiceTest;
import com.blocklang.release.service.InstallerService;

public class InstallerServiceImplTest extends AbstractServiceTest{

	@Autowired
	private InstallerService installerService;
	
	@Test
	public void save_success_one_row() {
		RegistrationInfo registrationInfo = new RegistrationInfo();
		registrationInfo.setAppRunPort(8080);
		registrationInfo.setArch("x86");
		registrationInfo.setIp("10.10.10.10");
		registrationInfo.setOsType("Ubuntu");
		registrationInfo.setOsVersion("19.04");
		registrationInfo.setRegistrationToken("registration_token");
		registrationInfo.setServerToken("server_01");
		registrationInfo.setTargetOs("Linux");
		
		String installerToken = installerService.save(registrationInfo, 1);
		assertThat(installerToken.length() <= 22, is(true));
		
		assertThat(countRowsInTable("WEB_SERVER"), is(1));
		assertThat(countRowsInTable("INSTALLER"), is(1));
	}
	
	@Test
	public void save_success_two_row_with_same_server() {
		RegistrationInfo registrationInfo = new RegistrationInfo();
		registrationInfo.setAppRunPort(8080);
		registrationInfo.setArch("x86");
		registrationInfo.setIp("10.10.10.10");
		registrationInfo.setOsType("Ubuntu");
		registrationInfo.setOsVersion("19.04");
		registrationInfo.setRegistrationToken("registration_token_1");
		registrationInfo.setServerToken("server_01");
		registrationInfo.setTargetOs("Linux");
		
		installerService.save(registrationInfo, 1);
		
		registrationInfo = new RegistrationInfo();
		registrationInfo.setAppRunPort(8081);
		registrationInfo.setArch("x86");
		registrationInfo.setIp("10.10.10.10");
		registrationInfo.setOsType("Ubuntu");
		registrationInfo.setOsVersion("19.04");
		registrationInfo.setRegistrationToken("registration_token_2");
		registrationInfo.setServerToken("server_01");
		registrationInfo.setTargetOs("Linux");
		
		installerService.save(registrationInfo, 2);
		
		assertThat(countRowsInTable("WEB_SERVER"), is(1));
		assertThat(countRowsInTable("INSTALLER"), is(2));
	}
}
