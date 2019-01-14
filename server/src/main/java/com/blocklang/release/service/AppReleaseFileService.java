package com.blocklang.release.service;

import java.util.Optional;

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
	 * @param targetOs TODO: 调整为 enum
	 * @param arch
	 * @return
	 */
	Optional<AppReleaseFile> find(int appReleaseId, String targetOs, String arch);

}
