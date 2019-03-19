package com.blocklang.core.service;

import java.util.Optional;

public interface DocumentService {

	Optional<String> findByFileName(String fileName);

}
