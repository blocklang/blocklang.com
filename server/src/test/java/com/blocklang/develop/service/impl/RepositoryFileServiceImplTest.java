package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.FileType;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.dao.RepositoryFileDao;
import com.blocklang.develop.dao.RepositoryResourceDao;
import com.blocklang.develop.model.RepositoryFile;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.RepositoryFileService;

public class RepositoryFileServiceImplTest extends AbstractServiceTest{

	@Autowired
	private RepositoryFileService repositoryFileService;
	
	@Autowired
	private RepositoryResourceDao repositorytResourceDao;
	@Autowired
	private RepositoryFileDao repositoryFileDao;
	
	@Test
	public void find_readme_no_data() {
		Optional<RepositoryFile> fileOption = repositoryFileService.findReadme(9999);
		assertThat(fileOption).isEmpty();
	}
	
	@Test
	public void find_readme_success() {
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(9999);
		resource.setKey(RepositoryResource.README_KEY);
		resource.setName(RepositoryResource.README_NAME);
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(RepositoryResourceType.FILE);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedProjectResourceId = repositorytResourceDao.save(resource).getId();
		
		RepositoryFile file = new RepositoryFile();
		file.setRepositoryResourceId(savedProjectResourceId);
		file.setContent("# Readme");
		file.setFileType(FileType.MARKDOWN);
		repositoryFileDao.save(file);
		
		Optional<RepositoryFile> fileOption = repositoryFileService.findReadme(9999);
		assertThat(fileOption).isPresent();
	}
}
