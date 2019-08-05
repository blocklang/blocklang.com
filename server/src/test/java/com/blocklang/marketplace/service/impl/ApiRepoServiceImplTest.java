package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.service.ApiRepoService;

public class ApiRepoServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ApiRepoService apiRepoService;
	@Autowired
	private ApiRepoDao apiRepoDao;
	
	@Test
	public void find_by_id_no_data() {
		assertThat(apiRepoService.findById(1)).isEmpty();
	}
	
	@Test
	public void find_by_id_no_success() {
		ApiRepo repo = new ApiRepo();
		repo.setGitRepoUrl("a");
		repo.setGitRepoWebsite("b");
		repo.setGitRepoOwner("c");
		repo.setGitRepoName("d");
		repo.setName("e");
		repo.setVersion("f");
		repo.setCategory(RepoCategory.CLIENT_API);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		ApiRepo savedRepo = apiRepoDao.save(repo);
		
		assertThat(apiRepoService.findById(savedRepo.getId())).isPresent();
	}
	
}
