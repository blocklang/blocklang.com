package com.blocklang.release.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
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
		// 这里使用 9999，因为初始数据已经使用 1 作为 appId
		Optional<App> appOption = appService.findById(9999);
		assertThat(appOption.isEmpty()).isTrue();
		
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		appDao.save(app);
		
		appOption = appService.findById(app.getId() + 1);
		
		assertThat(appOption).isEmpty();
	}
	
	@Test
	public void find_by_id_success() {
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		
		Optional<App> appOption = appService.findById(appDao.save(app).getId());
		assertThat(appOption).isPresent();
	}
	
	@Test
	public void find_by_project_id_no_data() {
		assertThat(appService.findByProjectId(1)).isEmpty();
	}
	
	@Test
	public void find_by_project_id_project_id_is_null() {
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		app.setProjectId(null);
		appDao.save(app);
		
		assertThat(appService.findByProjectId(1)).isEmpty();
	}
	
	@Test
	public void find_by_project_id_success() {
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		app.setProjectId(1);
		appDao.save(app);
		
		assertThat(appService.findByProjectId(1)).isPresent();
	}
	
	@Test
	public void find_by_name_no_data() {
		Optional<App> appOption = appService.findByAppName("not-exist-app-name");
		assertThat(appOption).isEmpty();
	}
	
	@Test
	public void find_by_name_success() {
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		appDao.save(app);
		
		Optional<App> appOption = appService.findByAppName("app");
		assertThat(appOption).isPresent();
	}
}
