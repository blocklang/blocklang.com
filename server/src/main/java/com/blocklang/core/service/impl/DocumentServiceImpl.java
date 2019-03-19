package com.blocklang.core.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;

import com.blocklang.core.service.DocumentService;

@Service
public class DocumentServiceImpl implements DocumentService {

	// 帮助文档放在 help 文件夹中管理
	@Override
	public Optional<String> findByFileName(String fileName) {
		try(InputStream io = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + fileName).openStream();) {
			String content = StreamUtils.copyToString(io, Charset.defaultCharset());
			return Optional.of(content);
		} catch (IOException e) {
			return Optional.empty();
		}
	}

}
