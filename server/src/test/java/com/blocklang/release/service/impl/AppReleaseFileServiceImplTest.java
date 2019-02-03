package com.blocklang.release.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.service.AbstractServiceTest;
import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.TargetOs;
import com.blocklang.release.dao.AppReleaseFileDao;
import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.service.AppReleaseFileService;

public class AppReleaseFileServiceImplTest extends AbstractServiceTest{

	@Autowired
	private AppReleaseFileService appReleaseFileService;
	@Autowired
	private AppReleaseFileDao appReleaseFileDao;
	
	@Test
	public void find_no_data() {
		Optional<AppReleaseFile> appReleaseFileOption = appReleaseFileService.find(1, TargetOs.WINDOWS, Arch.X86);
		assertThat(appReleaseFileOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_not_match_target_os() {
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setAppReleaseId(1);
		appReleaseFile.setTargetOs(TargetOs.LINUX);
		appReleaseFile.setArch(Arch.X86);
		appReleaseFile.setFileName("file_name");
		appReleaseFile.setFilePath("file_path");
		appReleaseFile.setCreateUserId(1);
		appReleaseFile.setCreateTime(LocalDateTime.now());
		
		appReleaseFileDao.save(appReleaseFile);
		
		Optional<AppReleaseFile> appReleaseFileOption = appReleaseFileService.find(1, TargetOs.WINDOWS, Arch.X86);
		assertThat(appReleaseFileOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_not_match_arch() {
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setAppReleaseId(1);
		appReleaseFile.setTargetOs(TargetOs.LINUX);
		appReleaseFile.setArch(Arch.X86);
		appReleaseFile.setFileName("file_name");
		appReleaseFile.setFilePath("file_path");
		appReleaseFile.setCreateUserId(1);
		appReleaseFile.setCreateTime(LocalDateTime.now());
		
		appReleaseFileDao.save(appReleaseFile);
		
		Optional<AppReleaseFile> appReleaseFileOption = appReleaseFileService.find(1, TargetOs.LINUX, Arch.X86_64);
		assertThat(appReleaseFileOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_not_match_target_os_and_arch() {
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setAppReleaseId(1);
		appReleaseFile.setTargetOs(TargetOs.LINUX);
		appReleaseFile.setArch(Arch.X86);
		appReleaseFile.setFileName("file_name");
		appReleaseFile.setFilePath("file_path");
		appReleaseFile.setCreateUserId(1);
		appReleaseFile.setCreateTime(LocalDateTime.now());
		
		appReleaseFileDao.save(appReleaseFile);
		
		Optional<AppReleaseFile> appReleaseFileOption = appReleaseFileService.find(1, TargetOs.WINDOWS, Arch.X86_64);
		assertThat(appReleaseFileOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_success_match_target_os_and_arch() {
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setAppReleaseId(1);
		appReleaseFile.setTargetOs(TargetOs.LINUX);
		appReleaseFile.setArch(Arch.X86);
		appReleaseFile.setFileName("file_name");
		appReleaseFile.setFilePath("file_path");
		appReleaseFile.setCreateUserId(1);
		appReleaseFile.setCreateTime(LocalDateTime.now());
		
		appReleaseFileDao.save(appReleaseFile);
		
		Optional<AppReleaseFile> appReleaseFileOption = appReleaseFileService.find(1, TargetOs.LINUX, Arch.X86);
		assertThat(appReleaseFileOption.isPresent(), is(true));
	}
	
	@Test
	public void find_success_match_target_os_any() {
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setAppReleaseId(1);
		appReleaseFile.setTargetOs(TargetOs.ANY);
		appReleaseFile.setArch(Arch.X86);
		appReleaseFile.setFileName("file_name");
		appReleaseFile.setFilePath("file_path");
		appReleaseFile.setCreateUserId(1);
		appReleaseFile.setCreateTime(LocalDateTime.now());
		
		appReleaseFileDao.save(appReleaseFile);
		
		Optional<AppReleaseFile> appReleaseFileOption = appReleaseFileService.find(1, TargetOs.LINUX, Arch.X86);
		assertThat(appReleaseFileOption.isPresent(), is(true));
	}
	
	@Test
	public void find_success_match_arch_any() {
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setAppReleaseId(1);
		appReleaseFile.setTargetOs(TargetOs.LINUX);
		appReleaseFile.setArch(Arch.ANY);
		appReleaseFile.setFileName("file_name");
		appReleaseFile.setFilePath("file_path");
		appReleaseFile.setCreateUserId(1);
		appReleaseFile.setCreateTime(LocalDateTime.now());
		
		appReleaseFileDao.save(appReleaseFile);
		
		Optional<AppReleaseFile> appReleaseFileOption = appReleaseFileService.find(1, TargetOs.LINUX, Arch.X86);
		assertThat(appReleaseFileOption.isPresent(), is(true));
	}
	
	@Test
	public void find_success_match_target_os_any_and_arch_any() {
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setAppReleaseId(1);
		appReleaseFile.setTargetOs(TargetOs.ANY);
		appReleaseFile.setArch(Arch.ANY);
		appReleaseFile.setFileName("file_name");
		appReleaseFile.setFilePath("file_path");
		appReleaseFile.setCreateUserId(1);
		appReleaseFile.setCreateTime(LocalDateTime.now());
		
		appReleaseFileDao.save(appReleaseFile);
		
		Optional<AppReleaseFile> appReleaseFileOption = appReleaseFileService.find(1, TargetOs.LINUX, Arch.X86);
		assertThat(appReleaseFileOption.isPresent(), is(true));
	}
	
	// 注意，要忽略大小写
	@Test
	public void find_success_ignore_case() {
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setAppReleaseId(1);
		appReleaseFile.setTargetOs(TargetOs.LINUX);
		appReleaseFile.setArch(Arch.X86);
		appReleaseFile.setFileName("file_name");
		appReleaseFile.setFilePath("file_path");
		appReleaseFile.setCreateUserId(1);
		appReleaseFile.setCreateTime(LocalDateTime.now());
		
		appReleaseFileDao.save(appReleaseFile);
		
		Optional<AppReleaseFile> appReleaseFileOption = appReleaseFileService.find(1, "LiNuX", "x86");
		assertThat(appReleaseFileOption.isPresent(), is(true));
	}
}
