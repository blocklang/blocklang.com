package com.blocklang.release.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.release.constant.ReleaseMethod;
import com.blocklang.release.dao.AppDao;
import com.blocklang.release.dao.AppReleaseDao;
import com.blocklang.release.model.App;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.service.AppReleaseService;

public class AppReleaseServiceImplTest extends AbstractServiceTest{

	@Autowired
	private AppReleaseService appReleaseService;
	@Autowired
	private AppReleaseDao appReleaseDao;
	@Autowired
	private AppDao appDao;
	
	@Test
	public void find_by_id_no_data() {
		Optional<AppRelease> appReleaseOption = appReleaseService.findById(1);
		assertThat(appReleaseOption.isEmpty()).isTrue();
	}
	
	@Test
	public void find_by_id_success() {
		AppRelease appRelease = new AppRelease();
		appRelease.setAppId(1);
		appRelease.setVersion("0.0.1");
		appRelease.setTitle("title");
		// 确定 clob 字段能存大量字符
		appRelease.setDescription(StringUtils.repeat("x", 3000));
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateUserId(1);
		appRelease.setCreateTime(LocalDateTime.now());
		
		Optional<AppRelease> appReleaseOption = appReleaseService.findById(appReleaseDao.save(appRelease).getId());
		assertThat(appReleaseOption).isPresent();
	}
	
	// 测试枚举类型的数据，存入数据库中的值是编码，而不是说明
	@Test
	public void save_success_check_enum_type() {
		AppRelease appRelease = new AppRelease();
		appRelease.setAppId(1);
		appRelease.setVersion("0.0.1");
		appRelease.setTitle("title");
		appRelease.setDescription("description");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateUserId(1);
		appRelease.setCreateTime(LocalDateTime.now());
		
		int appReleaseId = appReleaseDao.save(appRelease).getId();
		
		Map<String, Object> data = jdbcTemplate.queryForMap("SELECT * FROM APP_RELEASE WHERE DBID = ?", appReleaseId);
		assertThat(data.get("release_method")).isEqualTo("01");
	}
	
	@Test
	public void find_latest_release_app_no_data() {
		int appId = 1;
		Optional<AppRelease> appReleaseOption = appReleaseService.findLatestReleaseApp(appId);
		assertThat(appReleaseOption.isEmpty()).isTrue();
	}
	
	@Test
	public void find_latest_release_app_one_row_success() {
		int appId = 1;
		AppRelease appRelease = new AppRelease();
		appRelease.setAppId(appId);
		appRelease.setVersion("0.0.1");
		appRelease.setTitle("title");
		appRelease.setDescription("description");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateUserId(1);
		appRelease.setCreateTime(LocalDateTime.now());
		appReleaseDao.save(appRelease);
		
		Optional<AppRelease> appReleaseOption = appReleaseService.findLatestReleaseApp(appId);
		assertThat(appReleaseOption.get().getVersion()).isEqualTo("0.0.1");
	}
	
	@Test
	public void find_latest_release_app_two_row_success() {
		int appId = 1;
		AppRelease appRelease = new AppRelease();
		appRelease.setAppId(appId);
		appRelease.setVersion("0.0.1");
		appRelease.setTitle("title");
		appRelease.setDescription("description");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateUserId(1);
		appRelease.setCreateTime(LocalDateTime.now());
		appReleaseDao.save(appRelease);
		
		appRelease = new AppRelease();
		appRelease.setAppId(appId);
		appRelease.setVersion("0.0.2");
		appRelease.setTitle("title");
		appRelease.setDescription("description");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateUserId(1);
		appRelease.setCreateTime(LocalDateTime.now());
		appReleaseDao.save(appRelease);
		
		Optional<AppRelease> appReleaseOption = appReleaseService.findLatestReleaseApp(appId);
		assertThat(appReleaseOption.get().getVersion()).isEqualTo("0.0.2");
	}
	
	@Test
	public void find_by_appId_and_version_no_data() {
		Optional<AppRelease> appReleaseOption = appReleaseService.findByAppIdAndVersion(1, "0.1.0");
		assertThat(appReleaseOption).isEmpty();
	}
	
	@Test
	public void find_by_appId_and_version_success() {
		AppRelease appRelease = new AppRelease();
		appRelease.setAppId(1);
		appRelease.setVersion("0.0.1");
		appRelease.setTitle("title");
		appRelease.setDescription("description");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateUserId(1);
		appRelease.setCreateTime(LocalDateTime.now());
		appReleaseDao.save(appRelease);
		
		Optional<AppRelease> appReleaseOption = appReleaseService.findByAppIdAndVersion(1, "0.0.1");
		assertThat(appReleaseOption).isPresent();
	}

	@Test
	public void find_by_app_name_no_data() {
		assertThat(appReleaseService.findByAppName("a")).isEmpty();
	}
	
	@Test
	public void find_by_app_name_success() {
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		Integer appId = appDao.save(app).getId();
		
		AppRelease appRelease = new AppRelease();
		appRelease.setAppId(appId);
		appRelease.setVersion("0.0.1");
		appRelease.setTitle("title");
		appRelease.setDescription("description");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateUserId(1);
		appRelease.setCreateTime(LocalDateTime.now());
		appReleaseDao.save(appRelease);
		
		List<AppRelease> releases = appReleaseService.findByAppName("app");
		assertThat(releases).hasSize(1);
		assertThat(releases.get(0).getName()).isEqualTo("app");
		assertThat(releases.get(0).getVersion()).isEqualTo("0.0.1");
	}
	
	@Test
	public void find_by_app_name_order_by_release_time() {
		App app = new App();
		app.setAppName("app");
		app.setCreateUserId(1);
		app.setCreateTime(LocalDateTime.now());
		Integer appId = appDao.save(app).getId();
		
		AppRelease appRelease = new AppRelease();
		appRelease.setAppId(appId);
		appRelease.setVersion("0.0.1");
		appRelease.setTitle("title");
		appRelease.setDescription("description");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateUserId(1);
		appRelease.setCreateTime(LocalDateTime.now().minusSeconds(1));
		appReleaseDao.save(appRelease);
		
		appRelease = new AppRelease();
		appRelease.setAppId(appId);
		appRelease.setVersion("0.0.2");
		appRelease.setTitle("title2");
		appRelease.setDescription("description2");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateUserId(1);
		appRelease.setCreateTime(LocalDateTime.now().minusSeconds(1));
		appReleaseDao.save(appRelease);
		
		List<AppRelease> releases = appReleaseService.findByAppName("app");
		assertThat(releases).hasSize(2).isSortedAccordingTo(Comparator.comparing(AppRelease::getReleaseTime).reversed());
	}
}
