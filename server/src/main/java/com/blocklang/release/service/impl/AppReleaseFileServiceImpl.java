package com.blocklang.release.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.service.AppReleaseFileService;

@Service
public class AppReleaseFileServiceImpl implements AppReleaseFileService {

	@Override
	public Optional<AppReleaseFile> find(int appReleaseId, String targetOs, String arch) {
		// TODO Auto-generated method stub
		return null;
	}

}
