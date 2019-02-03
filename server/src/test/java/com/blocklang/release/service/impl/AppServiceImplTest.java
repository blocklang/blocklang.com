package com.blocklang.release.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.service.AbstractServiceTest;
import com.blocklang.release.dao.AppDao;
import com.blocklang.release.model.App;
import com.blocklang.release.service.AppService;

public class AppServiceImplTest extends AbstractServiceTest{

	@Autowired
	private AppService appService;
	
	@Autowired
	private AppDao appDao;
	
	@Test
	public void find_by_id_no_data() {
		Optional<App> appOption = appService.findById(1);
		assertThat(appOption.isEmpty(), is(true));
		
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		appDao.save(app);
		
		appOption = appService.findById(app.getId() + 1);
		assertThat(appOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_by_id_success() {
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		
		Optional<App> appOption = appService.findById(appDao.save(app).getId());
		assertThat(appOption.isPresent(), is(true));
	}

	@Test
	public void find_by_registration_token_no_data() {
		Optional<App> appOption = appService.findByRegistrationToken("not-exist-registration-token");
		assertThat(appOption.isEmpty(), is(true));
		
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		appDao.save(app);
		
		appOption = appService.findByRegistrationToken("not-exist-registration-token");
		assertThat(appOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_by_registration_token_success() {
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		app.setRegistrationToken("registration-token");
		appDao.save(app);
		
		Optional<App> appOption = appService.findByRegistrationToken("registration-token");
		assertThat(appOption.isPresent(), is(true));
	}
	
	@Test
	public void find_by_name_no_data() {
		Optional<App> appOption = appService.findByAppName("not-exist-app-name");
		assertThat(appOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_by_name_success() {
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		app.setRegistrationToken("registration-token");
		appDao.save(app);
		
		Optional<App> appOption = appService.findByAppName("app");
		assertThat(appOption.isPresent(), is(true));
	}
}
