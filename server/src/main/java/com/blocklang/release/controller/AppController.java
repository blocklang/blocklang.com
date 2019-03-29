package com.blocklang.release.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.release.constant.Constant;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.service.AppReleaseService;

@RestController
public class AppController {
	
	@Autowired
	private AppReleaseService appReleaseService;

	@GetMapping("/apps/jdk/releases")
	public ResponseEntity<List<AppRelease>> getJdks() {
		return ResponseEntity.ok(appReleaseService.findAllByAppName(Constant.JDK_APP_NAME));
	}
}
