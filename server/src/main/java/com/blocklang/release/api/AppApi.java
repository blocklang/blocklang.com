package com.blocklang.release.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.release.exception.ResourceNotFoundException;
import com.blocklang.release.model.App;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.service.AppReleaseFileService;
import com.blocklang.release.service.AppReleaseService;
import com.blocklang.release.service.AppService;

@RestController
@RequestMapping("/apps")
public class AppApi {
	
	@Autowired
	private AppService appService;
	
	@Autowired
	private AppReleaseService appReleaseService;
	
	@Autowired
	private AppReleaseFileService appReleaseFileService;

	@GetMapping
	public ResponseEntity<InputStreamSource> downloadAppFile(String appName, 
			String version, 
			String targetOs,
			String arch) {
		
		App app = appService.findByAppName(appName).orElseThrow(ResourceNotFoundException::new);
		
		AppRelease appRelease = appReleaseService.findByAppIdAndVersion(app.getId(), version)
				.orElseThrow(ResourceNotFoundException::new);
		
		AppReleaseFile appReleaseFile = appReleaseFileService.find(appRelease.getId(), targetOs, arch)
				.orElseThrow(ResourceNotFoundException::new);
		Path path = Paths.get(appReleaseFile.getFullPath());
		if(path.toFile().exists()) {
			try {
				InputStreamResource resource = new InputStreamResource(Files.newInputStream(path));
				return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
			} catch (IOException e) {
				throw new ResourceNotFoundException();
			}
		}
		
		throw new ResourceNotFoundException();
	}
}

//
//Path path = Paths.get(file.getAbsolutePath());
//ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
//
//return ResponseEntity.ok()
//        .headers(headers)
//        .contentLength(file.length())
//        .contentType(MediaType.parseMediaType("application/octet-stream"))
//        .body(resource);