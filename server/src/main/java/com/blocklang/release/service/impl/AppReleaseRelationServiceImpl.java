package com.blocklang.release.service.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.release.dao.AppReleaseRelationDao;
import com.blocklang.release.model.AppReleaseRelation;
import com.blocklang.release.service.AppReleaseRelationService;

@Service
public class AppReleaseRelationServiceImpl implements AppReleaseRelationService {
	
	private static final Logger logger = LoggerFactory.getLogger(AppReleaseRelationServiceImpl.class);

	@Autowired
	private AppReleaseRelationDao appReleaseRelationDao;
	
	@Override
	public Optional<Integer> findSingle(int appReleaseId) {
		List<AppReleaseRelation> result = appReleaseRelationDao.findByAppReleaseId(appReleaseId);
		if(result.isEmpty()) {
			return Optional.empty();
		}
		if(result.size() == 1) {
			return Optional.of(result.get(0).getDependAppReleaseId());
		}

		logger.error("期望最多找到一个依赖的 APP，但是却找到了 {} 个", result.size());
		return Optional.empty();
	}

}
