package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.FileType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.dao.ProjectFileDao;
import com.blocklang.develop.dao.ProjectResourceDao;
import com.blocklang.develop.model.ProjectFile;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectFileService;

public class ProjectFileServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ProjectFileService projectFileService;
	
	@Autowired
	private ProjectResourceDao projectResourceDao;
	@Autowired
	private ProjectFileDao projectFileDao;
	
	@Test
	public void find_readme_no_data() {
		Optional<ProjectFile> fileOption = projectFileService.findReadme(9999);
		assertThat(fileOption).isEmpty();
	}
	
	@Test
	public void find_readme_success() {
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(9999);
		resource.setKey(ProjectResource.README_KEY);
		resource.setName(ProjectResource.README_NAME);
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(ProjectResourceType.FILE);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedProjectResourceId = projectResourceDao.save(resource).getId();
		
		ProjectFile file = new ProjectFile();
		file.setProjectResourceId(savedProjectResourceId);
		file.setContent("# Readme");
		file.setFileType(FileType.MARKDOWN);
		projectFileDao.save(file);
		
		Optional<ProjectFile> fileOption = projectFileService.findReadme(9999);
		assertThat(fileOption).isPresent();
	}
}
