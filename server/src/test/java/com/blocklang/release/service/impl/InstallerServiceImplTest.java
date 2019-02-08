package com.blocklang.release.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.OsType;
import com.blocklang.release.dao.InstallerDao;
import com.blocklang.release.dao.WebServerDao;
import com.blocklang.release.data.NewRegistrationParam;
import com.blocklang.release.data.UpdateRegistrationParam;
import com.blocklang.release.model.Installer;
import com.blocklang.release.model.WebServer;
import com.blocklang.release.service.InstallerService;

public class InstallerServiceImplTest extends AbstractServiceTest{

	@Autowired
	private InstallerService installerService;
	@Autowired
	private InstallerDao installerDao;
	@Autowired
	private WebServerDao webServerDao;
	
	@Test
	public void save_success_one_row() {
		NewRegistrationParam registrationInfo = new NewRegistrationParam();
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
		NewRegistrationParam registrationInfo = new NewRegistrationParam();
		registrationInfo.setAppRunPort(8080);
		registrationInfo.setArch("x86");
		registrationInfo.setIp("10.10.10.10");
		registrationInfo.setOsType("Ubuntu");
		registrationInfo.setOsVersion("19.04");
		registrationInfo.setRegistrationToken("registration_token_1");
		registrationInfo.setServerToken("server_01");
		registrationInfo.setTargetOs("Linux");
		
		installerService.save(registrationInfo, 1);
		
		registrationInfo = new NewRegistrationParam();
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
	
	@Test
	public void update_success() {
		WebServer webServer = new WebServer();
		webServer.setArch(Arch.X86);
		webServer.setIp("10.10.10.10");
		webServer.setOsType(OsType.WINDOWS);
		webServer.setOsVersion("v1");
		webServer.setServerToken("server_token");
		webServer.setCreateUserId(1);
		webServer.setCreateTime(LocalDateTime.now());
		WebServer existedWebServer = webServerDao.save(webServer);
		
		Installer installer = new Installer();
		installer.setAppReleaseId(1);
		installer.setAppRunPort(80);
		installer.setInstallerToken("installer_token");
		installer.setWebServerId(existedWebServer.getId());
		installer.setCreateUserId(1);
		installer.setCreateTime(LocalDateTime.now());
		Installer existedInstaller = installerDao.save(installer);
		
		UpdateRegistrationParam registrationInfo = new UpdateRegistrationParam();
		registrationInfo.setAppRunPort(8080);
		registrationInfo.setArch(Arch.X86_64.getValue());
		registrationInfo.setInstallerToken("installer_token");
		registrationInfo.setIp("11.11.11.11");
		registrationInfo.setOsType(OsType.LINUX.getValue());
		registrationInfo.setOsVersion("v2");
		registrationInfo.setServerToken("server_Token");
		
		installerService.update(existedInstaller, registrationInfo);
		
		WebServer updatedWebServer = webServerDao.findById(existedWebServer.getId()).get();
		assertThat(updatedWebServer.getArch(), equalTo(Arch.X86_64));
		assertThat(updatedWebServer.getOsType(), equalTo(OsType.LINUX));
		assertThat(updatedWebServer.getOsVersion(), equalTo("v2"));
		assertThat(updatedWebServer.getIp(), equalTo("11.11.11.11"));
		
		Installer updatedInstaller = installerDao.findById(existedInstaller.getId()).get();
		assertThat(updatedInstaller.getAppRunPort(), is(8080));
		
	}

	@Test
	public void find_by_installer_token_no_data() {
		Optional<Installer> installerOption = installerDao.findByInstallerToken("not-exist-installer-token");
		assertThat(installerOption.isEmpty(), is(true));
	}
	
	@Test
	public void delete_success() {
		Installer installer = new Installer();
		installer.setAppReleaseId(1);
		installer.setAppRunPort(80);
		installer.setInstallerToken("token");
		installer.setWebServerId(1);
		installer.setCreateTime(LocalDateTime.now());
		installer.setCreateUserId(1);
		Installer existedInstaller = installerDao.save(installer);
		assertThat(countRowsInTable("INSTALLER"), is(1));
		installerService.delete(existedInstaller);
		installerDao.flush();
		assertThat(countRowsInTable("INSTALLER"), is(0));
	}
}
