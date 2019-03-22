package com.blocklang.develop.service;

import java.util.Optional;

import com.blocklang.develop.model.ProjectFile;

public interface ProjectFileService {

	Optional<ProjectFile> findReadme(Integer projectId);

}
