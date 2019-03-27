package com.blocklang.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.dao.UserBindDao;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.service.UserBindService;
import com.blocklang.core.test.AbstractServiceTest;

public class UserBindServiceImplTest extends AbstractServiceTest{

	@Autowired
	private UserBindService userBindService;
	
	@Autowired
	private UserBindDao userBindDao;
	
	@Test
	public void find_by_site_and_openid_no_data() {
		assertThat(userBindService.findBySiteAndOpenId(OauthSite.GITHUB, "1")).isEmpty();
	}
	
	@Test
	public void find_by_site_and_openid_success() {
		UserBind userBind = new UserBind();
		userBind.setCreateTime(LocalDateTime.now());
		userBind.setSite(OauthSite.GITHUB);
		userBind.setOpenId("2");
		userBind.setUserId(1);
		userBindDao.save(userBind);
		
		assertThat(userBindService.findBySiteAndOpenId(OauthSite.GITHUB, "2")).isPresent();
	}
}
