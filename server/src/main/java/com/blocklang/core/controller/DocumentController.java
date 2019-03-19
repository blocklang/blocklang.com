package com.blocklang.core.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.service.DocumentService;

@RestController
public class DocumentController {
	
	@Autowired
	private DocumentService documentService;

	@GetMapping("/docs/{fileName}")
	public ResponseEntity<String> getDocument(
			@PathVariable("fileName") String fileName) {
		if(StringUtils.isBlank(fileName)) {
			fileName = "getting-started";
		}
		
		String filePath = "help/" + fileName + "/index.md";
		
		String content = documentService.findByFileName(filePath).orElseThrow(ResourceNotFoundException::new);
		
		// 转换 markdown 中的图片链接
		// ![首页](./images/home.png)
		content = content.replace("](./", "](/raw/docs/" + fileName + "/");
		
		return ResponseEntity.ok(content);
	}
	
	@GetMapping("/raw/docs/**")
	public ResponseEntity<InputStreamSource> getRawFile(HttpServletRequest req) {
		String strPath = SpringMvcUtil.getRestUrl(req, 2);
		String filePath = "help/" + strPath;
		
		try {
			InputStream io = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + filePath).openStream();
			InputStreamResource resource = new InputStreamResource(io);
			// 目前只支持 png
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
		} catch (IOException e) {
			throw new ResourceNotFoundException();
		}
	}
}
