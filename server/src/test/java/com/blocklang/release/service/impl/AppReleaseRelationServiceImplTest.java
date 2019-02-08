package com.blocklang.release.service.impl;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.release.dao.AppReleaseRelationDao;
import com.blocklang.release.model.AppReleaseRelation;
import com.blocklang.release.service.AppReleaseRelationService;

public class AppReleaseRelationServiceImplTest extends AbstractServiceTest {

	@Autowired
	private AppReleaseRelationService appReleaseRelationService;
	@Autowired
	private AppReleaseRelationDao appReleaseRelationDao;
	
	@Test
	public void find_single_no_data() {
		Optional<Integer> dependAppReleaseIdOption = appReleaseRelationService.findSingle(1);
		assertThat(dependAppReleaseIdOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_single_success_one_row() {
		int appReleaseId = 1;
		AppReleaseRelation appReleaseRelation = new AppReleaseRelation();
		appReleaseRelation.setAppReleaseId(appReleaseId);
		appReleaseRelation.setDependAppReleaseId(2);
		appReleaseRelationDao.save(appReleaseRelation);
		
		Optional<Integer> dependAppReleaseIdOption = appReleaseRelationService.findSingle(appReleaseId);
		assertThat(dependAppReleaseIdOption.get(), is(2));
	}
	
	@Test
	public void find_single_success_two_row() {
		int appReleaseId = 1;
		AppReleaseRelation appReleaseRelation = new AppReleaseRelation();
		appReleaseRelation.setAppReleaseId(appReleaseId);
		appReleaseRelation.setDependAppReleaseId(2);
		appReleaseRelationDao.save(appReleaseRelation);
		
		appReleaseRelation = new AppReleaseRelation();
		appReleaseRelation.setAppReleaseId(appReleaseId);
		appReleaseRelation.setDependAppReleaseId(3);
		appReleaseRelationDao.save(appReleaseRelation);
		
		Optional<Integer> dependAppReleaseIdOption = appReleaseRelationService.findSingle(appReleaseId);
		assertThat(dependAppReleaseIdOption.isEmpty(), is(true));
	}
}
