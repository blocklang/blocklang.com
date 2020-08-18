package com.blocklang.develop.service;

import java.util.Optional;

import com.blocklang.develop.model.RepositoryFile;

public interface RepositoryFileService {

	Optional<RepositoryFile> findReadme(Integer repositoryId);

}
