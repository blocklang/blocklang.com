package com.blocklang.release.service.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.TargetOs;
import com.blocklang.release.dao.AppReleaseFileDao;
import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.service.AppReleaseFileService;

@Service
public class AppReleaseFileServiceImpl implements AppReleaseFileService {

	private static final Logger logger = LoggerFactory.getLogger(AppReleaseFileServiceImpl.class);
	@Autowired
	private AppReleaseFileDao appReleaseFileDao;

	@Override
	public Optional<AppReleaseFile> find(int appReleaseId, TargetOs targetOs, Arch arch) {
		List<AppReleaseFile> appReleaseFiles = appReleaseFileDao.findByAppReleaseId(appReleaseId);
		
		// 先精准匹配
		Optional<AppReleaseFile> result = appReleaseFiles
				.stream()
				.filter((each) -> each.getTargetOs() == targetOs && each.getArch() == arch)
				.findAny();
		if(result.isPresent()) {
			return result;
		}
		
		result = appReleaseFiles
				.stream()
				.filter((each) -> each.getTargetOs() == TargetOs.ANY && each.getArch() == arch)
				.findAny();
		if(result.isPresent()) {
			return result;
		}
		
		result = appReleaseFiles
				.stream()
				.filter((each) -> each.getTargetOs() == targetOs && each.getArch() == Arch.ANY)
				.findAny();
		if(result.isPresent()) {
			return result;
		}
		
		result = appReleaseFiles
				.stream()
				.filter((each) -> each.getTargetOs() == TargetOs.ANY && each.getArch() == Arch.ANY)
				.findAny();
		
		return result;
	}

	@Override
	public Optional<AppReleaseFile> find(int appReleaseId, String targetOsValue, String archValue) {
		TargetOs targetOsEnum = TargetOs.fromValue(targetOsValue);
		if(targetOsEnum == TargetOs.UNKNOWN) {
			logger.warn("在 TargetOs 编码中，根据编码值 {}，没有查找到对应的编码", targetOsValue);
		}
		Arch archEnum = Arch.fromValue(archValue);
		if(archEnum == Arch.UNKNOWN) {
			logger.warn("在 Arch 编码中，根据编码值 {}，没有查找到对应的编码", archValue);
		}
		return this.find(appReleaseId, targetOsEnum, archEnum);
	}

}
