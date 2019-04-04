package com.blocklang.release.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.data.domain.Range;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.util.RangeHeader;
import com.blocklang.develop.model.AppGlobalContext;
import com.blocklang.release.model.App;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.service.AppReleaseFileService;
import com.blocklang.release.service.AppReleaseService;
import com.blocklang.release.service.AppService;
import com.nimbusds.oauth2.sdk.util.StringUtils;

@RestController
@RequestMapping("/apps")
public class AppApi {
	
	private static final Logger logger = LoggerFactory.getLogger(AppApi.class);
	
	@Autowired
	private AppService appService;
	@Autowired
	private AppReleaseService appReleaseService;
	@Autowired
	private AppReleaseFileService appReleaseFileService;
	@Autowired
	private PropertyService propertyService;

	@GetMapping
	public ResponseEntity<InputStreamSource> downloadAppFile(String appName, 
			String version, 
			String targetOs,
			String arch,
			@RequestHeader(required = false) String range,
			@RequestHeader(required = false, name = "if-range") String ifRange) {
		
		App app = appService.findByAppName(appName).orElseThrow(ResourceNotFoundException::new);
		
		AppRelease appRelease = appReleaseService.findByAppIdAndVersion(app.getId(), version)
				.orElseThrow(ResourceNotFoundException::new);
		
		AppReleaseFile appReleaseFile = appReleaseFileService.find(appRelease.getId(), targetOs, arch)
				.orElseThrow(ResourceNotFoundException::new);

		String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).get();
		String mavenRootPath = propertyService.findStringValue(CmPropKey.MAVEN_ROOT_PATH).get();
		
		AppGlobalContext context = new AppGlobalContext(dataRootPath, mavenRootPath);
		if(app.getProjectId() == null) {
			// 如果是平台使用的软件，则从 apps 文件夹中获取文件
			// 只存储紧密相关的相对路径，如不存储 apps 文件夹
			appReleaseFile.setAbsoluteRootPath(context.getAppsDirectory().toString());
		} else {
			// 如果是 blocklang 中的项目生成的 jar，则从 maven 目录结构中获取
			appReleaseFile.setAbsoluteRootPath(context.getMavenRepositoryRootDirectory().toString());
		}

		Path path = Paths.get(appReleaseFile.getFullPath());
		File file = path.toFile();
		if(file.exists()) {
			// 结合 if-range 和 range 判断是否走断点续传
			// 如果 if-range 校验不通过，说明文件发生了变化，需要重新下载。
			String fileMd5 = null;
			try {
				fileMd5 = DigestUtils.md5DigestAsHex(Files.newInputStream(path));
			} catch (IOException e) {
				logger.warn("为文件生成 md5 时出错", e);
			}
			
			if(StringUtils.isNotBlank(ifRange) && ifRange.equals(fileMd5) && RangeHeader.isValid(range)) {
				long fileLength = file.length();
				// 断点续传
				Range<Long> parsedRange = RangeHeader.parse(range, fileLength);
				// 目前只支持 range=1- 格式
				try {
					InputStream in = Files.newInputStream(path);
					long started = parsedRange.getLowerBound().getValue().get();
					long skiped = 0;
					while (started - skiped > 0) {
						skiped += in.skip(started);
					}
					InputStreamResource resource = new InputStreamResource(in);
					// 设置文件名
					return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
							.contentLength(fileLength - started) // 注意，断点续传时，此处是剩余的内容长度
							.contentType(MediaType.APPLICATION_OCTET_STREAM)
							.header(HttpHeaders.ACCEPT_RANGES, "bytes")
							.header(HttpHeaders.CONTENT_RANGE, "bytes " + started + "-" + (fileLength - 1) + "/" + fileLength)
							.header(HttpHeaders.CONTENT_DISPOSITION,"attachment;fileName=" + appReleaseFile.getFileName())
							.body(resource);
				} catch (IOException e) {
					throw new ResourceNotFoundException();
				}
			}else {
				try {
					InputStreamResource resource = new InputStreamResource(Files.newInputStream(path));
					// 设置文件名
					return ResponseEntity.ok()
							.contentLength(file.length())
							.contentType(MediaType.APPLICATION_OCTET_STREAM)
							.header(HttpHeaders.CONTENT_DISPOSITION,"attachment;fileName=" + appReleaseFile.getFileName())
							.header(HttpHeaders.ETAG, fileMd5)
							.body(resource);
				} catch (IOException e) {
					throw new ResourceNotFoundException();
				}
			}
		}
		
		throw new ResourceNotFoundException();
	}
}
