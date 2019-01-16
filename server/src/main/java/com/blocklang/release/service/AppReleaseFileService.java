package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.TargetOs;
import com.blocklang.release.model.AppReleaseFile;

/**
 * APP 发行版文件逻辑服务接口
 * 
 * @author Zhengwei Jin
 *
 */
public interface AppReleaseFileService {

	/**
	 * 获取发行版文件，如果找不到准确匹配的，就获取跨平台版本。
	 * 
	 * @param appReleaseId
	 * @param targetOs
	 * @param arch
	 * @return 获取发行版文件
	 */
	Optional<AppReleaseFile> find(int appReleaseId, TargetOs targetOs, Arch arch);
	
	/**
	 * 
	 * @param appReleaseId
	 * @param targetOsValue 注意，targetOs 是经过编码的，这里传入的是 value，而不是 key
	 * @param archValue 注意，arch 是经过编码的，这里传入的是 value，而不是 key
	 * @return
	 */
	Optional<AppReleaseFile> find(int appReleaseId, String targetOsValue, String archValue);

}
